package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "freelancer_member")
public class FreelancerEntity {

    @Id
    @Column(name = "freelancer_id")
    private String freelancerId;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", referencedColumnName = "member_id", insertable = false, updatable = false)
    private MemberEntity member;

	@Column(name = "freelancer_phone", length = 100)
	private String freelancerPhone;
	
	@Column(name = "postal_code", length = 20)
	private String postalCode;
	
	@Column(name = "address", length = 100)
	private String address;
	
	@Column(name = "detail_address", length = 100)
	private String detailAddress;	
	
	@Column(name = "extra_address", length = 100)
	private String extraAddress;

	@Column(name = "country", length = 100)
	private String country;
}