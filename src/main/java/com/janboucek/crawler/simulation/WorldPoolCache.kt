package com.janboucek.crawler.simulation

import org.jbox2d.dynamics.World
import org.jbox2d.pooling.IWorldPool
import org.jbox2d.pooling.normal.DefaultWorldPool

/*
* a cache which keeps a world pool for every thread to reduce allocation on world creation
* */
object WorldPoolCache {
    private val cache = mutableMapOf<Long, IWorldPool>()

    fun getPool(): IWorldPool {
        val threadId = Thread.currentThread().id
        cache[threadId]?.let { return it }
        val pool = DefaultWorldPool(World.WORLD_POOL_SIZE, World.WORLD_POOL_CONTAINER_SIZE)
        cache[threadId] = pool
        return pool
    }
}