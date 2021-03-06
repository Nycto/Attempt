package com.roundeights.attempt

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure => TryFailure}

/**
 * An 'OnFail' is a behavior to execute if another clause fails
 */
object OnFail {

    /** Creates a new OnFail */
    def apply ( onFailure: => Unit )
        = new OnFail( () => onFailure )

    /** Creates a new OnFail that receives a failed value */
    def call[F: Manifest] ( onFailure: () => Unit )
        = new OnFail( onFailure )

    /** Creates a new OnFail that receives a failed value */
    def call[F: Manifest] ( onFailure: (F) => Unit )
        = new OnFailWith( onFailure )

    /** Fails a promise when an action fails */
    def alsoFail ( promise: Promise[_] )
        = new OnFailWith[Throwable]( (err) => promise.failure( err) )

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
        val result = Promise[A]()
        condition.onComplete {
            case TryFailure(_) => {
                try {
                    onFailure()
                    result.completeWith( condition )
                }
                catch {
                    case err: Throwable => result.failure(err)
                }
            }
            case _ => result.completeWith( condition )
        }
        result.future
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


