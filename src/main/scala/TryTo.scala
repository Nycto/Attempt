package com.roundeights.attempt

import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.util.{Failure => TryFailure}

/**
 * TryTo is a mechanism for executing a given behavior if a value fails
 */
object TryTo {

    /** Binds an Option to a failure method */
    def apply[S] ( condition: Option[S] ) = new TryTo[Option[S]] {

        /** {@inheritDoc} */
        override def onFail ( failure: => Unit ): Option[S] = {
            condition match {
                case Some(_) => condition
                case None => {
                    failure
                    condition
                }
            }
        }
    }

    /** Binds a Boolean to a failure method */
    def apply ( condition: Boolean ) = new TryTo[Option[Boolean]] {

        /** {@inheritDoc} */
        override def onFail ( failure: => Unit ): Option[Boolean] = {
            condition match {
                case true => Some(true)
                case false => {
                    failure
                    None
                }
            }
        }
    }

    /** Binds an Either to a failure method */
    def apply[F,S] (
        condition: Either[F,S]
    ) = new TryToWith[Option[S],F] {

        /** {@inheritDoc} */
        override def onFailMatch (
            failure: PartialFunction[F,Unit]
        ): Option[S] = {
            condition match {
                case Right(value) => Some(value)
                case Left(value) => {
                    if ( failure.isDefinedAt(value) )
                        failure(value)
                    None
                }
            }
        }
    }

    /** Binds a Future to a failure method */
    def apply[S]
        ( condition: Future[S] )
        ( implicit executor: ExecutionContext )
    = new TryToWith[Future[S],Throwable] {

        /** {@inheritDoc} */
        override def onFailMatch (
            failure: PartialFunction[Throwable,Unit]
        ): Future[S] = {
            val result = Promise[S]()

            condition.onComplete {
                case TryFailure(err) if failure.isDefinedAt(err) => {
                    try {
                        failure( err )
                        result.completeWith( condition )
                    }
                    catch {
                        case thrown: Throwable => result.failure(thrown)
                    }
                }
                case _ => result.completeWith( condition )
            }

            result.future
        }
    }

    /** Binds a value that might throw an exception to a failure method */
    def except[S] ( condition: => S ) = new TryToWith[Option[S],Throwable] {

        /** {@inheritDoc} */
        override def onFailMatch (
            failure: PartialFunction[Throwable,Unit]
        ): Option[S] = {
            try {
                Some( condition )
            } catch {
                case err: Throwable if failure.isDefinedAt(err) => {
                    failure(err)
                    None
                }
            }
        }
    }

}

/** The interface for a executing callback if a value fails */
trait TryTo[S] {

    /** Executes the given thunk when the TryTo fails */
    def onFail ( failure: => Unit ): S
}

/** The interface for executing a callback if a value fails */
trait TryToWith[S,F] extends TryTo[S] {

    /** {@inheritDoc} */
    override def onFail ( failure: => Unit ): S
        = onFailMatch { case _ => failure }

    /** Executes the given thunk when the TryTo fails */
    def onFailMatch ( failure: PartialFunction[F,Unit] ): S
}

