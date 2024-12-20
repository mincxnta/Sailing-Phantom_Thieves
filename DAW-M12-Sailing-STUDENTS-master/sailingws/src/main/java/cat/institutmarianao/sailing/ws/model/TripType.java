package cat.institutmarianao.sailing.ws.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/* JPA annotations */
@Entity
@Table(name = "trip_types")
/* Lombok */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripType implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String GROUP = "GROUP";
	public static final String PRIVATE = "PRIVATE";

	public enum Category {
		GROUP, PRIVATE
	}

	/* Validation */
	/* JPA */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/* Lombok */
	@EqualsAndHashCode.Include
	@Column
	private Long id;

	/* Validation */
	@Column
	private String title;

	/* Validation */
	/* JPA */
	@Column
	@Enumerated(EnumType.STRING)
	private Category category;

	/* Validation */
	@Column
	private String description;

	/* Validation */
	@Column
	private double price;

	/* JPA */
	@ElementCollection
	@CollectionTable(name = "trip_type_departures", joinColumns = @JoinColumn(name = "id"))
	@Temporal(TemporalType.TIME)
	@Column(name = "departure")
	private List<Date> departures;

	/* Validation */
	@Column
	private int duration;

	/* Validation */
	/* JPA */
	@Column(name = "max_places")
	private int maxPlaces;
}
