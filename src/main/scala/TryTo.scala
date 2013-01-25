package com.roundeights.attempt

import scala.concurrent.{Future, ExecutionContext}

/**
 * TryTo is a mechanism for executing a given behavior if a value fails
 */
object TryTo {

    /** Binds an Option to a failure method */
    def apply[S] ( condition: Option[S] ) = new TryTo[Option[S]] {

        /** {@inheritDoc} */
        override def onFailCall ( failure: () => Unit ): Option[S] = {
            condition match {
                case Some(_) => condition
                case None => {
                    failure()
                    condition
                }
            }
        }
    }

    /** Binds a Boolean to a failure method */
    def apply ( condition: Boolean ): TryTo[Option[Boolean]] = {
        condition match {
            case true => apply( Some(true) )
            case false => apply( None )
        }
    }

    /** Binds an Either to a failure method */
    def apply[F,S] (
        condition: Either[F,S]
    ) = new TryToWith[Option[S],F] {

        /** {@inheritDoc} */
        override def onFailCall ( failure: (F) => Unit ): Option[S] = {
            condition match {
                case Right(value) => Some(value)
                case Left(value) => {
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
        override def onFailCall ( failure: (Throwable) => Unit ): Future[S] = {
            condition.onFailure {
                case err: Throwable => failure(err)
            }
            condition
        }

    }

    /** Binds a value that might throw an exception to a failure method */
    def except[S] ( condition: => S ) = new TryToWith[Option[S],Throwable] {

        /** {@inheritDoc} */
        override def onFailCall ( failure: (Throwable) => Unit ): Option[S] = {
            try {
                Some( condition )
            } catch {
                case err: Throwable => {
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
    def onFailCall ( failure: () => Unit ): S

    /** Executes the given thunk when the TryTo fails */
    def onFail ( failure: => Unit ): S = onFailCall( () => failure )
}

/** The interface for executing a callback if a value fails */
trait TryToWith[S,F] extends TryTo[S] {

    /** {@inheritDoc} */
    override def onFailCall ( failure: () => Unit ): S
        = onFailCall( (_) => failure() )

    /** Executes the given thunk when the TryTo fails */
    def onFailCall ( failure: (F) => Unit ): S
}

