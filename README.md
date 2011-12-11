Attempt
=======

Attempt is a micro-library for Scala that makes it possible to use an
`Either`-like structure in sequence comprehensions. The idea is to make the
logic for accumulating data easier to read, while providing custom error
messaging at each phase. If one step fails, all the rest are automatically
short circuited.

It's easier to see an example:

    def process (
        requestData: Map[String, String]
    ): Either[String, Product] = {

        import com.roundeights.attempt.Attempt

        val response = for {

            userID <- Attempt(
                // Calling 'get' on a map returns an Option
                requestData.get("userID"),
                "RequestData is missing the 'userID' key"
            )

            user <- Attempt(
                // findByUserID should return an Option
                findUserByID( userID ),
                "User could not be found"
            )

            productID <- Attempt(
                // Calling 'get' on a map returns an Option
                requestData.get("productID"),
                "RequestData is missing the 'productID' key"
            )

            product <- Attempt(
                // getPurchase should return an Option
                user.getPurchase("productID"),
                "User has not purchased that product"
            )

        } yield product

    }

