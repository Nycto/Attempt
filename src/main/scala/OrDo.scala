package com.roundeights.attempt

import scala.concurrent.{Future, ExecutionContext}

/**
 * An 'OrDo' is a behavior to execute if another clause fails
 */
object OrDo {

    /** Creates a new 'OrDo' */
    def apply ( onFailure: => Unit ) = new OrDo( () => onFailure )

}


/**
 * A OrDo is a behavior to execute if another clause fails
 */
class OrDo ( private val onFailure: () => Unit ) {

    /** Binds this behavior to an option */
    def :: [A] ( condition: Option[A] ): Option[A] = condition match {
        case Some(_) => condition
        case None => {
            onFailure()
            None
        }
    }

    /** Binds this behavior to an Either */
    def :: [A, B] ( condition: Either[A, B] ): Either[A, B] = condition match {
        case Right(_) => condition
        case Left(_) => {
            onFailure()
            condition
        }
    }

    /** Binds this behavior to a Future */
    def :: [A]
        ( condition: Future[A] )
        ( implicit executor: ExecutionContext )
    : Future[A] = {
        condition.onFailure { case _ => onFailure() }
        condition
    }

}

