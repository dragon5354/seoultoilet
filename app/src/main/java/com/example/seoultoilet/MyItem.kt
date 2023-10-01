package com.example.seoultoilet

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * ClusterItem을 구현하는 클래스
 * GetSnippet(), getTitle(), GetPosition() 함수를 구현
 * 생성자에서 전달받은 데이터를 반환할 수 있도록 구현
 * 멤버 프로퍼티는 인터페이스 Getter와 이름이 다르게 지정
 * 마커의 아이콘을 변경하기 위해 icon 파라미터를 추가로 받음
 */
class MyItem(
    val _position: LatLng,
    val _title: String,
    val _snippet: String,
    val _icon: BitmapDescriptor
) :
    ClusterItem {

    override fun getSnippet(): String {
        return _snippet
    }

    override fun getTitle(): String {
        return _title
    }

    override fun getPosition(): LatLng {
        return _position
    }

    fun getIcon(): BitmapDescriptor {
        return _icon
    }

    // 검색에서 아이템을 찾기 위해 동등성 함수 override
    // GPS상 위도, 경도, 제목, 설명이 모두 같을 시 같은 객체(화장실)로 취급
    override fun equals(other: Any?) : Boolean {
        if(other is MyItem) {
            return (other.position.latitude == position.latitude
                    && other.position.longitude == position.longitude
                    && other.title == _title
                    && other.snippet == _snippet)
        }
        return false
    }

    // equals()를 오버라이드한 경우 반드시 오버라이드 필요
    // 같은 객체는 같은 해시코드를 반환해야 함.
    override fun hashCode(): Int {
        var hash = _position.latitude.hashCode() * 31
        hash = hash * 31 + _position.longitude.hashCode()
        hash = hash * 31 + title.hashCode()
        hash = hash * 31 + snippet.hashCode()
        return hash
    }
}