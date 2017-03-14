/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package results_viewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.jbox2d.common.Vec2;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.TestbedTest;
import org.jbox2d.testbed.framework.TestbedModel.TestChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*********************************************************************
 *This class is just a modified copy of the TestbedController,       *
 *introduces some fixes in order to improve stability of the GUI     *
 *********************************************************************/

/**
 * This class contains most control logic for the testbed and the update loop. It also watches the
 * model to switch tests and populates the model with some loop statistics.
 *
 * @author Daniel Murphy
 */

public class FixedController implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FixedController.class);
    public static final int DEFAULT_FPS = 60;
    private TestbedTest currTest = null;
    private TestbedTest nextTest = null;
    private long startTime;
    private long frameCount;
    private int targetFrameRate;
    private float frameRate = 0.0F;
    private boolean animating = false;
    private Thread animator;
    private final TestbedModel model;
    private final TestbedPanel panel;
    private TestbedController.UpdateBehavior updateBehavior;

    public FixedController(TestbedModel argModel, TestbedPanel argPanel, TestbedController.UpdateBehavior behavior) {
        this.model = argModel;
        this.setFrameRate(60);
        this.panel = argPanel;
        this.animator = new Thread(this, "Testbed");
        this.updateBehavior = behavior;
        this.addListeners();
    }

    private void addListeners() {
        this.model.addTestChangeListener(new TestChangedListener() {
            public void testChanged(TestbedTest argTest, int argIndex) {
                FixedController.this.nextTest = argTest;
                FixedController.this.panel.grabFocus();
            }
        });
        this.panel.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                char key = e.getKeyChar();
                int code = e.getKeyCode();
                if (code >= 512) {
                    return;
                }
                if (key != '\uffff') {
                    FixedController.this.model.getKeys()[key] = false;
                }

                FixedController.this.model.getCodedKeys()[code] = false;
                if (FixedController.this.model.getCurrTest() != null) {
                    FixedController.this.model.getCurrTest().queueKeyReleased(key, code);
                }

            }

            public void keyPressed(KeyEvent e) {
                char key = e.getKeyChar();
                int code = e.getKeyCode();
                if (code >= 512) {
                    return;
                }
                if (key != '\uffff') {
                    FixedController.this.model.getKeys()[key] = true;
                }

                FixedController.this.model.getCodedKeys()[code] = true;
                if (key == 32 && FixedController.this.model.getCurrTest() != null) {
                    FixedController.this.model.getCurrTest().lanchBomb();
                } else if (key == 91) {
                    FixedController.this.lastTest();
                } else if (key == 93) {
                    FixedController.this.nextTest();
                } else if (key == 114) {
                    FixedController.this.resetTest();
                } else if (FixedController.this.model.getCurrTest() != null) {
                    FixedController.this.model.getCurrTest().queueKeyPressed(key, code);
                }

            }
        });
        this.panel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (FixedController.this.model.getCurrTest() != null) {
                    Vec2 pos = new Vec2((float) e.getX(), (float) e.getY());
                    FixedController.this.model.getDebugDraw().getScreenToWorldToOut(pos, pos);
                    FixedController.this.model.getCurrTest().queueMouseUp(pos);
                }

            }

            public void mousePressed(MouseEvent e) {
                FixedController.this.panel.grabFocus();
                if (FixedController.this.model.getCurrTest() != null) {
                    Vec2 pos = new Vec2((float) e.getX(), (float) e.getY());
                    if (e.getButton() == 1) {
                        FixedController.this.model.getDebugDraw().getScreenToWorldToOut(pos, pos);
                        FixedController.this.model.getCurrTest().queueMouseDown(pos);
                        if (FixedController.this.model.getCodedKeys()[16]) {
                            FixedController.this.model.getCurrTest().queueShiftMouseDown(pos);
                        }
                    }
                }

            }
        });
        this.panel.addMouseMotionListener(new MouseMotionListener() {
            final Vec2 posDif = new Vec2();
            final Vec2 pos = new Vec2();
            final Vec2 pos2 = new Vec2();

            public void mouseDragged(MouseEvent e) {
                this.pos.set((float) e.getX(), (float) e.getY());
                if (e.getButton() == 3) {
                    this.posDif.set(FixedController.this.model.getMouse());
                    FixedController.this.model.setMouse(this.pos);
                    this.posDif.subLocal(this.pos);
                    if (!FixedController.this.model.getDebugDraw().getViewportTranform().isYFlip()) {
                        this.posDif.y *= -1.0F;
                    }

                    FixedController.this.model.getDebugDraw().getViewportTranform().getScreenVectorToWorld(this.posDif, this.posDif);
                    FixedController.this.model.getDebugDraw().getViewportTranform().getCenter().addLocal(this.posDif);
                    if (FixedController.this.model.getCurrTest() != null) {
                        FixedController.this.model.getCurrTest().setCachedCameraPos(FixedController.this.model.getDebugDraw().getViewportTranform().getCenter());
                    }
                }

                if (FixedController.this.model.getCurrTest() != null) {
                    FixedController.this.model.setMouse(this.pos);
                    FixedController.this.model.getDebugDraw().getScreenToWorldToOut(this.pos, this.pos);
                    FixedController.this.model.getCurrTest().queueMouseMove(this.pos);
                }

            }

            public void mouseMoved(MouseEvent e) {
                this.pos2.set((float) e.getX(), (float) e.getY());
                FixedController.this.model.setMouse(this.pos2);
                if (FixedController.this.model.getCurrTest() != null) {
                    FixedController.this.model.getDebugDraw().getScreenToWorldToOut(this.pos2, this.pos2);
                    FixedController.this.model.getCurrTest().queueMouseMove(this.pos2);
                }

            }
        });
    }

    protected void loopInit() {
        this.panel.grabFocus();
        if (this.currTest != null) {
            this.currTest.init(this.model);
        }

    }

    protected void update() {
        if (this.currTest != null && this.updateBehavior == TestbedController.UpdateBehavior.UPDATE_CALLED && currTest.getModel() != null) {
            this.currTest.update();
        }

    }

    public void nextTest() {
        int index = this.model.getCurrTestIndex() + 1;

        for (index %= this.model.getTestsSize(); !this.model.isTestAt(index) && index < this.model.getTestsSize() - 1; ++index) {
            ;
        }

        if (this.model.isTestAt(index)) {
            this.model.setCurrTestIndex(index);
        }

    }

    public void resetTest() {
        this.model.getCurrTest().reset();
    }

    public void saveTest() {
        this.model.getCurrTest().save();
    }

    public void loadTest() {
        this.model.getCurrTest().load();
    }

    public void lastTest() {
        int index = this.model.getCurrTestIndex() - 1;

        for (index = index < 0 ? index + this.model.getTestsSize() : index; !this.model.isTestAt(index) && index > 0; --index) {
            ;
        }

        if (this.model.isTestAt(index)) {
            this.model.setCurrTestIndex(index);
        }

    }

    public void playTest(int argIndex) {
        if (argIndex != -1) {
            while (!this.model.isTestAt(argIndex)) {
                if (argIndex + 1 >= this.model.getTestsSize()) {
                    return;
                }

                ++argIndex;
            }

            this.model.setCurrTestIndex(argIndex);
        }
    }

    public void setFrameRate(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("Fps cannot be less than or equal to zero");
        } else {
            this.targetFrameRate = fps;
            this.frameRate = (float) fps;
        }
    }

    public int getFrameRate() {
        return this.targetFrameRate;
    }

    public float getCalculatedFrameRate() {
        return this.frameRate;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getFrameCount() {
        return this.frameCount;
    }

    public boolean isAnimating() {
        return this.animating;
    }

    int id;

    public synchronized void start(int id) {
        this.id = id;
        if (!this.animating) {
            this.frameCount = 0L;
            this.animator.start();
        } else {
            log.warn("Animation is already animating.");
        }

    }

    public synchronized void stop() {
        this.animating = false;
        this.animator.interrupt();
    }

    public void run() {
        long updateTime;
        long beforeTime = this.startTime = updateTime = System.nanoTime();
        long sleepTime = 0L;
        this.animating = true;
        this.loopInit();

        for (; this.animating; beforeTime = System.nanoTime()) {
            if (animator.isInterrupted()) break;
            //System.out.println(id);
            if (this.nextTest != null) {
                this.nextTest.init(this.model);
                this.model.setRunningTest(this.nextTest);
                if (this.currTest != null) {
                    this.currTest.exit();
                }

                this.currTest = this.nextTest;
                this.nextTest = null;
            }

            long timeSpent = beforeTime - updateTime;
            if (timeSpent > 0L) {
                float timeInSecs = (float) timeSpent * 1.0F / 1.0E9F;
                updateTime = System.nanoTime();
                this.frameRate = this.frameRate * 0.9F + 1.0F / timeInSecs * 0.1F;
                this.model.setCalculatedFps(this.frameRate);
            } else {
                updateTime = System.nanoTime();
            }

            if (this.panel.render()) {
                this.update();
                this.panel.paintScreen();
            }

            ++this.frameCount;
            long afterTime = System.nanoTime();
            long timeDiff = afterTime - beforeTime;
            sleepTime = ((long) (1000000000 / this.targetFrameRate) - timeDiff) / 1000000L;
            if (sleepTime > 0L) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException var15) {
                    ;
                }
            }
        }

    }

    public static enum UpdateBehavior {
        UPDATE_CALLED,
        UPDATE_IGNORED;

        private UpdateBehavior() {
        }
    }
}
