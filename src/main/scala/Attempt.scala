/**
 * Attempts are just like Eithers, but they are usable in for comprehensions.
 * This allows you to do things like accumulate data in a readable manner,
 * but produce custom error messages at each step.
 */

package com.roundeights.attempt

/**
 * Compaion for generating an Attempt
 */
object Attempt {

    /**
     * Thrown when attempting to fail a value that can not be failed
     */
    case class Unfailable() extends Exception

    /**
     * Create an attempt from an Option
     */
    def apply[S, F] ( condition: Option[S], onError: => F ): Attempt[S, F]
        = condition match {
            case None => Failure( onError )
            case Some(value) => new Success(value, () => onError)
        }

    /**
     * Create an attempt from a boolean expression
     */
    def apply[F] ( condition: Boolean, onError: => F ): Attempt[Boolean, F]
        = condition match {
            case false  => Failure( onError )
            case true => new Success( true, () => onError )
        }

    /**
     * Creates an always successful attempt
     */
    def apply[S] ( value: S ): Attempt[S, Nothing]
        = new Success( value, () => throw Unfailable() )

    /**
     * Wraps an attempt in a try/catch
     */
    def except[E <: Exception, S, F] (
        condition: => S, onError: => F
    ): Attempt[S, F]
        = try {
            new Success( condition, () => onError )
        } catch {
            case _: E => Failure( onError )
        }

    /**
     * Converts an Attempt to an Either
     */
    implicit def attemptToEither[S, F] ( attempt: Attempt[S, F] ): Either[F, S]
        = attempt match {
            case Success(value) => Right(value)
            case Failure(error) => Left(error)
        }

    /**
     * Converts an Attempt to an Option
     */
    implicit def attemptToOption[S, F] ( attempt: Attempt[S, F] ): Option[S]
        = attempt match {
            case Success(value) => Some(value)
            case Failure(_) => None
        }

}

/**
 * The base class for the Attempts. Think of this as an 'Either'
 *
 * This sets up the methods needed to use an Attempt in a for comprehension.
 * For more information about this, see chapter 10 of "Scala by Example",
 * which can be found here:
 * http://www.scala-lang.org/docu/files/ScalaByExample.pdf
 */
abstract sealed class Attempt [+S, +F] {

    /**
     * Applies a callback to this Attempt if it was successful. The result
     * from the callback will be translated into an Attempt
     */
    def map[N] ( callback: S => N ): Attempt[N, F]

    /**
     * Applies a callback to this Attempt if it was successful. The callback
     * should return another attempt
     */
    def flatMap[NS, NF >: F] ( callback: S => Attempt[NS, NF] ): Attempt[NS, NF]

    /**
     * Applies a callback to this Attempt if it was successful
     */
    def foreach[U] ( callback: S => U ): Unit

    /**
     * Filters this attempt according to a predicate
     */
    def withFilter ( predicate: S => Boolean ): Attempt[S, F]

}

/**
 * Companion for the Success object
 */
object Success {

    /**
     * Create a new Success object
     */
    def apply[S, F]( success: S, failure: => F ): Success[S,F]
        = new Success[S, F]( success, () => failure )

    /**
     * Extracts the value from a success object
     */
    def unapply[S, F]( success: Success[S, F] ): Some[S] = Some(success.value)

}

/**
 * A success link in the Attempt. Think of this as a 'Right'
 */
class Success[S, F] (
    val value: S, val failure: () => F
) extends Attempt[S, F] with Equals {

    /** {@inheritDoc} */
    override def map[N] ( callback: S => N ): Attempt[N, F]
        = new Success[N, F]( callback(value), failure )

    /** {@inheritDoc} */
    override def flatMap[NS, NF >: F] (
        callback: S => Attempt[NS, NF]
    ): Attempt[NS, NF]
        = callback( value )

    /** {@inheritDoc} */
    override def foreach[U] ( callback: S => U ): Unit
        = callback( value )

    /** {@inheritDoc} */
    override def withFilter ( predicate: S => Boolean ): Attempt[S, F]
        = if ( predicate(value) ) this else Failure[S, F]( failure() )

    /** {@inheritDoc} */
    override def toString (): String = "Success(%s)".format( value.toString )

    /** {@inheritDoc} */
    override def hashCode (): Int = value.hashCode

    /** {@inheritDoc} */
    override def canEqual(other: Any): Boolean
        = other.isInstanceOf[Success[_,_]]

    /** {@inheritDoc} */
    override def equals(other: Any) = other match {
        case vs: Success[_, _] => (vs.canEqual(this)) && (value == vs.value)
        case _ => false
    }

}

/**
 * A failure link in the Attempt. Think of this as a 'Left'
 */
case class Failure[S, F] ( val failure: F ) extends Attempt[S, F] {

    /** {@inheritDoc} */
    override def map[N] ( callback: S => N ): Attempt[N, F]
        = Failure[N, F] ( failure )

    /** {@inheritDoc} */
    override def flatMap[NS, NF >: F] (
        callback: S => Attempt[NS, NF]
    ): Attempt[NS, NF]
        = Failure[NS, F] ( failure )

    /** {@inheritDoc} */
    override def foreach[U] ( callback: S => U ): Unit = {}

    /** {@inheritDoc} */
    override def withFilter ( predicate: S => Boolean ): Attempt[S, F] = this

}


