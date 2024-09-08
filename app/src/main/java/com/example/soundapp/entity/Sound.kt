package com.example.soundapp.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sounds",
    foreignKeys = [ForeignKey(
        entity = SoundGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class Sound(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val groupId: Int,
    var progress: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(groupId)
        parcel.writeInt(progress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sound> {
        override fun createFromParcel(parcel: Parcel): Sound {
            return Sound(parcel)
        }

        override fun newArray(size: Int): Array<Sound?> {
            return arrayOfNulls(size)
        }
    }
}
