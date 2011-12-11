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

    "Implicit conversion for an Attempt" should {

        "Convert a Failure to a Left" in {
            val result: Either[String, String] = Failure[String,String]("Fail")
            result must_== Left("Fail")
        }

        "Convert a Success to a Right" in {
            val result: Either[String, String] = Success[String,String]("Pass")
            result must_== Right("Pass")
        }

        "Convert a Failure to a None" in {
            val result: Option[String] = Failure[String,String]("Fail")
            result must_== None
        }

        "Convert a Success to a Right" in {
            val result: Option[String] = Success[String,String]("Pass")
            result must_== Some("Pass")
        }

    }

}
