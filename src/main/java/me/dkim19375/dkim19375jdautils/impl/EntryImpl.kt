package me.dkim19375.dkim19375jdautils.impl

import me.dkim19375.dkim19375jdautils.annotation.API

@API
class EntryImpl<K, V>(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {
    override fun setValue(newValue: V): V {
        val oldValue = this.value
        this.value = newValue
        return oldValue
    }
}