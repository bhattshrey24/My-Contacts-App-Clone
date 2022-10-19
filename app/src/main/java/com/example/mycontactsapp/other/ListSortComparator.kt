package com.example.mycontactsapp.other

import com.example.mycontactsapp.data.models.Contact

class ListSortComparator : Comparator<Contact?> {
    override fun compare(p0: Contact?, p1: Contact?): Int {
        return if (p0 == null && p1 == null) {
            0 // i.e. both are null so consider them duplicate
        } else if (p0 == null || p1 == null) { // one of them is null
            if (p0 == null) {
                +1 // i.e. o2 comes before o1
            } else {
                -1 // i.e. o1 comes before o2
            }
        } else {// both are not null
            val n1 = p0.name // Now name can also be null therefore again i'll do same null checks
            val n2 = p1.name
            if (n1 == null && n2 == null) {
                0
            } else if (n1 == null || n2 == null) {
                if (n1 == null) {
                    +1
                } else {
                    -1
                }
            } else {
                n1.compareTo(n2)
            }
        }
    }

}