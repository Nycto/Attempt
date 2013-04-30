package com.roundeights.attempt

import scala.concurrent._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import java.util.concurrent.Executor

/**
 * A few helpers for testing concurrent code
 */
object FutureTester {

    /** Blocks while waiting for the given future */
    def await[T] ( future: Future[T] ): T
        = Await.result( future, Duration(10, "second") )

    /** An execution context that runs in the calling thread */
    implicit val context = ExecutionContext.fromExecutor(new Executor {
        override def execute( command: Runnable ): Unit = command.run
    })

}

