package com.kubuci.hort.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "`group`")
public class Group {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 160, nullable = false)
	private String name;

	//@ManyToOne(fetch = FetchType.LAZY, optional = false)
	//@JoinColumn(name = "id_tutor", nullable = false, foreignKey = @ForeignKey(name = "fk_group_tutor"))
	//private Tutor tutor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//public Tutor getTutor() {
	//	return tutor;
	//}

	//public void setTutor(Tutor tutor) {
	//	this.tutor = tutor;
	//}
}
