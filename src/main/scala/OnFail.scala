package com.roundeights.attempt

import scala.concurrent.{Future, ExecutionContext}

/**
 * An 'OnFail' is a behavior to execute if another clause fails
 */
object OnFail {

    /** Creates a new OnFail */
    def apply ( onFailure: => Unit )
        = new OnFail( () => onFailure )

    /** Creates a new OnFail that receives a failed value */
    def apply[F: Manifest] ( onFailure: (F) => Unit )
        = new OnFailWith( onFailure )

    /** Creates a new OnFail that receives a failed value */
    def apply[F: Manifest] ( onFailure: PartialFunction[F,Unit] )
        = new OnFailWith( onFailure.apply )

}

/**
 * An OnFail is a behavior to execute if another clause fails
 */
class OnFail ( private val onFailure: () => Unit ) {

    /** Binds this behavior to a boolean value */
    def :: ( condition: Boolean ): Option[Boolean] = condition match {
        case true => Some(true)
        case false => {
            onFailure()
            None
        }
    }

    /** Binds this behavior to an option */
    def :: [A] ( condition: Option[A] ): Option[A] = condition match {
        case Some(_) => condition
        case None => {
            onFailure()
            None
        }
    }

    /** Binds this behavior to an Either */
    def :: [A, B] ( condition: Either[A,B] ): Option[B] = condition match {
        case Right(value) => Some(value)
        case Left(_) => {
            onFailure()
            None
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

/**
 * An OnFail is a behavior to execute if another clause fails
 */
class OnFailWith[F: Manifest] ( private val onFailure: (F) => Unit ) {

    /** Binds this behavior to an Either */
    def :: [S] ( condition: Either[F,S] ): Option[S] = condition match {
        case Right(value) => Some(value)
        case Left(value) => {
            onFailure(value)
            None
        }
    }

    /** Binds this behavior to a Future */
    def :: [A]
        ( condition: Future[A] )
        ( implicit executor: ExecutionContext )
    : Future[A] = {
        condition.onFailure { case err: F => onFailure(err) }
        condition
    }

}


