package com.github.jntakpe.availability

import com.github.jntakpe.commons.context.CommonsConstants.BASE_PACKAGE
import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
    build()
        .args(*args)
        .packages("$BASE_PACKAGE.availability")
        .start()
}

