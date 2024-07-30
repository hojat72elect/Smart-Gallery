package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Event
import androidx.annotation.RequiresApi
import com.simplemobiletools.gallery.pro.extensions.contactsDB
import com.simplemobiletools.gallery.pro.extensions.getEmptyContact
import com.simplemobiletools.gallery.pro.models.SimpleContact
import com.simplemobiletools.gallery.pro.models.contacts.Contact
import com.simplemobiletools.gallery.pro.models.contacts.Group
import com.simplemobiletools.gallery.pro.models.contacts.LocalContact
import com.simplemobiletools.gallery.pro.models.contacts.Organization

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class LocalContactsHelper(val context: Context) {
    fun getAllContacts(favoritesOnly: Boolean = false): ArrayList<Contact> {
        val contacts =
            if (favoritesOnly) context.contactsDB.getFavoriteContacts() else context.contactsDB.getContacts()
        val storedGroups = ContactsHelper(context).getStoredGroupsSync()
        return (contacts.map { convertLocalContactToContact(it, storedGroups) }
            .toMutableList() as? ArrayList<Contact>) ?: arrayListOf()
    }


    private fun convertLocalContactToContact(
        localContact: LocalContact?,
        storedGroups: ArrayList<Group>
    ): Contact? {
        if (localContact == null) {
            return null
        }

        val contactPhoto = if (localContact.photo == null) {
            null
        } else {
            try {
                BitmapFactory.decodeByteArray(localContact.photo, 0, localContact.photo!!.size)
            } catch (e: OutOfMemoryError) {
                null
            }
        }

        return context.getEmptyContact().apply {
            id = localContact.id!!
            prefix = localContact.prefix
            firstName = localContact.firstName
            middleName = localContact.middleName
            surname = localContact.surname
            suffix = localContact.suffix
            nickname = localContact.nickname
            phoneNumbers = localContact.phoneNumbers
            emails = localContact.emails
            addresses = localContact.addresses
            events = localContact.events
            source = SMT_PRIVATE
            starred = localContact.starred
            contactId = localContact.id!!
            thumbnailUri = ""
            photo = contactPhoto
            photoUri = localContact.photoUri
            notes = localContact.notes
            groups = storedGroups.filter { localContact.groups.contains(it.id) } as ArrayList<Group>
            organization = Organization(localContact.company, localContact.jobPosition)
            websites = localContact.websites
            IMs = localContact.IMs
            ringtone = localContact.ringtone
        }
    }

}
