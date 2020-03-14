package com.epam.brn.csv.converter

import com.epam.brn.constant.BrnInitFiles

interface FileNameToUploader {
    fun getUploaderFor(initFile: BrnInitFiles): Uploader<out Any, out Any>?
}
