package cat.institutmarianao.sailing.ws.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cat.institutmarianao.sailing.ws.SailingWsApplication;
import cat.institutmarianao.sailing.ws.error.utils.ErrorUtils;
import cat.institutmarianao.sailing.ws.exception.ForbiddenException;
import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Booking;
import cat.institutmarianao.sailing.ws.model.Cancellation;
import cat.institutmarianao.sailing.ws.model.Done;
import cat.institutmarianao.sailing.ws.model.Rescheduling;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.User.Role;
import cat.institutmarianao.sailing.ws.repository.ActionRepository;
import cat.institutmarianao.sailing.ws.service.ActionService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
@Service
public class ActionServiceImpl implements ActionService {
	@Autowired
	private ActionRepository actionRepository;

	@Override
	public List<Action> findByTripId(Long id) {
		return actionRepository.findByTripId(id);
	}

	@Override
	public Action save(@NotNull @Valid Action action) throws ParseException {
		Status status = action.getTrip().getStatus();
		if (action instanceof Booking) {
			throw new ConstraintViolationException("Trip is already booked", null);
		}

		if (status.equals(Status.CANCELLED) || status.equals(Status.DONE)) {
			throw new ConstraintViolationException("Trip is finished", null);
		}

		SimpleDateFormat sdf = new SimpleDateFormat(SailingWsApplication.DATE_PATTERN);
		Date tripDate = action.getTrip().getDate();
		Date today = sdf.parse(sdf.format(new Date()));

		long days = tripDate.getTime() - today.getTime();
		long hours = Duration.ofMillis(days).toHours();

		if (action instanceof Cancellation && hours < 48) {
			throw new ConstraintViolationException("Trips can only be cancelled up to 48h in advance", null);
		}

		if (!action.getPerformer().equals(action.getTrip().getClient())
				&& action.getPerformer().getRole().equals(Role.CLIENT)) {
			throw new ForbiddenException("You can't change another client's trip status");
		}

		if (action instanceof Done && action.getDate().before(action.getTrip().getDate())) {
			throw new ForbiddenException("You can't finish a future trip");
		}

		if (action instanceof Rescheduling rescheduling) {
			ErrorUtils.checkDepartures(rescheduling.getNewDeparture(), rescheduling.getTrip());
		}

		return actionRepository.saveAndFlush(action);
	}

}
