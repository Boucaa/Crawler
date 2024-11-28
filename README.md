Crawler
-------

Crawler is a HyperNEAT implementation used to train controllers of legged robots simulated in 2D. It started as a school leaving examination IT project and will hopefully receive some improvements in the future. The full documentation/paper can be found [here (Czech)](https://github.com/Boucaa/Crawler/raw/master/documentation/crawler.pdf).

A pre-built package with trained controllers can be found in the releases [here](https://github.com/Boucaa/Crawler/releases).

### Results ###
The best and some of the more interesting results can be seen on my Youtube channel:
[![click here to see video](https://img.youtube.com/vi/TC0XdPDnJrQ/0.jpg)](https://www.youtube.com/watch?v=TC0XdPDnJrQ)

### Building ###
The project can be built using the IntelliJ IDEA IDE, a Maven build coming soon (probably). The only requirement is a JDK with JavaFX support (OpenJDK will not work).

### Running ###
The project consists of 2 jars:

main - the HyperNEAT training algorithm  
gui - a results viewer  

It is recommended to run them both in the same folder, as they use the same results folder.

### References ###
#### libraries: ####
[JBox2D](https://github.com/jbox2d/jbox2d)  - used for simulation and vizualization

#### papers: ####
A Hypercube-Based Encoding for Evolving Large-Scale Neural Networks, Stanley et al., 2009

Evolving Coordinated Quadruped Gaits with the HyperNEAT
Generative Encoding, Clune et al., 2009

Evolving Neural Networks through Augmenting Topologies, Stanley et al., 2002  
