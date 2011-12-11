package test.roundeights.attempt

import org.specs2.mutable._

object AttemptTest extends Specification {

    import com.roundeights.attempt._

    "Attempts within a list comprehension" should {

        "yield the final value if all the errors are successful" in {
            val result = for {
                a <- Attempt( Some("abc"), "Error 1" )
                b <- Attempt( Some("xyz" + a), "Error 2" )
            } yield b

            result must_== Success("xyzabc")
        }

        "yield an intermediary error if anything is None" in {
            val result = for {
                a <- Attempt( None, "Error 1" )
                b <- Attempt( Some("xyz" + a), "Error 2" )
            } yield b

            result must_== Failure("Error 1")
        }

    }

}
