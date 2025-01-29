package cat.institutmarianao.sailing.controllers;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import cat.institutmarianao.sailing.model.Action;
import cat.institutmarianao.sailing.model.Cancellation;
import cat.institutmarianao.sailing.model.Done;
import cat.institutmarianao.sailing.model.Rescheduling;
import cat.institutmarianao.sailing.model.Trip;
import cat.institutmarianao.sailing.model.TripType;
import cat.institutmarianao.sailing.services.TripService;
import cat.institutmarianao.sailing.validation.groups.OnActionCreate;
import cat.institutmarianao.sailing.validation.groups.OnTripCreate;
import cat.institutmarianao.sailing.validation.groups.OnTripCreateDate;
import cat.institutmarianao.sailing.validation.groups.OnTripCreateDeparture;
import jakarta.validation.constraints.Positive;

@Controller
@SessionAttributes({ "trip", "tripType", "freePlaces", "tripFreePlaces" })
@RequestMapping(value = "/trips")
public class TripController {

	@Autowired
	private TripService tripService;

	@ModelAttribute("trip")
	private Trip setupTrip() {
		return new Trip();
	}

	@ModelAttribute("tripType")
	private TripType setupTripType() {
		return new TripType();
	}

	@ModelAttribute("freePlaces")
	private Map<Date, Long> setupFreePlaces() {
		return new HashMap<>();
	}

	@ModelAttribute("tripFreePlaces")
	private Long setupTripFreePlaces() {
		return 0l;
	}

	@GetMapping("/book/{trip_type_id}")
	public ModelAndView bookSelectDate(@PathVariable(name = "trip_type_id", required = true) Long tripTypeId) {
		TripType triptype = tripService.getTripTypeById(tripTypeId);
		ModelAndView modelview = new ModelAndView("book_date");
		modelview.getModelMap().addAttribute(triptype);
		return modelview;
	}

	@PostMapping("/book/book_departure")
	public String bookSelectDeparture(@Validated(OnTripCreateDate.class) @ModelAttribute("trip") Trip trip,
			BindingResult result, @SessionAttribute("tripType") TripType tripType,
			@SessionAttribute("freePlaces") Map<Date, Long> freePlaces, ModelMap modelMap) {

		// TODO - Prepare a dialog to select a departure time for the booked trip
		// TODO - Leave all free places for the selected trip in the selected departure
		// date in session (freePlaces attribute)
		return null;
	}

	@PostMapping("/book/book_places")
	public String bookSelectPlaces(@Validated(OnTripCreateDeparture.class) @ModelAttribute("trip") Trip trip,
			BindingResult result, @SessionAttribute("tripType") TripType tripType,
			@SessionAttribute("freePlaces") Map<Date, Long> freePlaces,
			@SessionAttribute("tripFreePlaces") Long tripFreePlaces, ModelMap modelMap) {

		// TODO - Prepare a dialog to select places for the booked trip
		return null;
	}

	@PostMapping("/book/book_save")
	public String bookSave(@Validated(OnTripCreate.class) @ModelAttribute("trip") Trip trip, BindingResult result,
			@SessionAttribute("tripType") TripType tripType, @SessionAttribute("freePlaces") Map<Date, Long> freePlaces,
			@SessionAttribute("tripFreePlaces") Long tripFreePlaces, ModelMap modelMap, SessionStatus sessionStatus) {
		// Torna a la vista de selecció de places si hi ha errors
		if (result.hasErrors()) {
			return "book_places";
		}
		Trip savedTrip = tripService.save(trip);
		// Fa falta?
		sessionStatus.setComplete();
		return "redirect:/trips/booked";
	}

	@GetMapping("/booked")
	public ModelAndView booked() {
		// Obtenim l'usuari autenticat
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		ModelAndView modelview = new ModelAndView("trips");

		if (authorities.stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"))) {
			modelview.getModelMap().addAttribute("booked_trips", tripService.findAll());
		} else {
			modelview.getModelMap().addAttribute("booked_trips",
					tripService.findAllByClientUsername(authentication.getName()));
		}
		// Mapa devolviendo triptype i triptypeid
		return modelview;
	}

	// Com s’indica de quin viatge és?
	@PostMapping("/cancel")
	public String cancelTrip(@Validated Cancellation cancellation) {
		tripService.track(cancellation);
		return "redirect:/trips/booked";
	}

	@PostMapping("/done")
	public String doneTrip(@Validated(OnActionCreate.class) Done done) {
		tripService.track(done);
		return "redirect:/trips/booked";
	}

	@PostMapping("/reschedule")
	public String saveAction(@Validated(OnActionCreate.class) Rescheduling rescheduling) {
		tripService.track(rescheduling);
		return "redirect:/trips/booked";
	}

	@GetMapping("/tracking/{id}")
	public String showContentPart(@PathVariable(name = "id", required = true) @Positive Long id, ModelMap modelMap) {
		List<Action> tracking = tripService.findTrackingById(id);
		modelMap.addAttribute("tracking", tracking);
		return "trips";

	}
}
