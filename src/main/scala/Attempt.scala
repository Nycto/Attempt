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
     * Create an attempt
     */
    def apply[S, F] ( condition: Option[S], onError: => F ) = condition match {
        case None => Failure( onError )
        case Some(value) => Success(value)
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
abstract sealed class Attempt [+S, +F] () {

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
    def foreach ( callback: S => Unit ): Unit

}

/**
 * A success link in the Attempt. Think of this as a 'Right'
 */
case class Success[S, F] ( val value: S ) extends Attempt[S, F] {

    /** {@inheritDoc} */
    override def map[N] ( callback: S => N ): Attempt[N, F]
        = Success[N, F]( callback(value) )

    /** {@inheritDoc} */
    override def flatMap[NS, NF >: F] (
        callback: S => Attempt[NS, NF]
    ): Attempt[NS, NF]
        = callback( value )

    /** {@inheritDoc} */
    override def foreach ( callback: S => Unit ): Unit
        = callback( value )

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
    override def foreach ( callback: S => Unit ): Unit = {}

}


