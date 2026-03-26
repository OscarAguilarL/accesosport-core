package com.accesosport.shared.domain.i18n;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageKeys {

	public static class AuthMessages {
		private AuthMessages() {
		}

		public static final String AUTHENTICATION_FAILED = "auth.errors.auth-failed";
		public static final String EMAIL_ALREADY_EXISTS = "auth.errors.email-already-exists";
		public static final String PASSWORD_LENGTH_ERROR = "auth.errors.password-length-error";
		public static final String PASSWORDS_NOT_MATCH = "auth.errors.password-not-match";
		public static final String ROLE_NOT_FOUND = "auth.errors.role-not-found";
		public static final String EMAIl_REQUIRED = "auth.errors.email-required";
		public static final String PASSWORD_REQUIRED = "auth.errors.password-required";
		public static final String INVALID_EMAIL = "auth.errors.invalid-email";
		public static final String PASSWORD_CONFIRMATION_REQUIRED = "auth.errors.password-confirmation-required";
		public static final String ROLE_REQUIRED = "auth.errors.role-required";
		public static final String USER_NOT_FOUND = "auth.errors.user-not-found";
		public static final String INVALID_CREDENTIALS = "auth.errors.invalid-credentials";

		public static final String PROBLEM_USER_NOT_FOUND = "auth.problem.user.not-found";
		public static final String PROBLEM_USER_ALREADY_EXISTS = "auth.problem.user.already-exists";
		public static final String PROBLEM_INVALID_CREDENTIALS = "auth.problem.auth.invalid-credentials";
		public static final String PROBLEM_AUTH_ERROR = "auth.problem.auth.error";
		public static final String PROBLEM_ROLE_NOT_FOUND = "auth.problem.role.not-found";
		public static final String PROBLEM_VALIDATION_ERROR = "auth.problem.validation.error";
		public static final String PROBLEM_VALIDATION_FAILED = "auth.problem.validation.failed";
	}

	public static class Users {
		private Users() {
		}

		public static final String USER_PROFILE_PARTICIPANT_ALREADY_HAS_ROLE = "user.profile.participant.already-has-role";
		public static final String USER_PROFILE_PARTICIPANT_ALREADY_HAS_PROFILE = "user.profile.participant.already-has-profile";
		public static final String USER_PROFILE_ORGANIZER_ALREADY_HAS_ROLE = "user.profile.organizer.already-has-role";
		public static final String USER_PROFILE_ORGANIZER_ALREADY_HAS_PROFILE = "user.profile.organizer.already-has-profile";
		public static final String USER_PROFILE_ORGANIZER_VERIFICATION_INVALID_STATUS = "user.profile.organizer.verification.invalid-status";

		public static final String USER_PROFILE_ORGANIZER_NOT_FOUND = "No pudimos encontrar el perfil de organizador especificado";
		public static final String USER_PROFILE_PARTICIPANT_NOT_FOUND = "No pudimos encontrar el perfil de participante especificado";
		public static final String ROLE_NOT_FOUND = "user.role.not-found";
		public static final String PERSONAL_DATA_NOT_FOUND = "user.personal-data.not-found";

		public static final String USER_ADDRESS_VALIDATION_STREET_REQUIRED = "user.address.validation.street.required";
		public static final String USER_ADDRESS_VALIDATION_STREET_LENGTH = "user.address.validation.street.length";
		public static final String USER_ADDRESS_VALIDATION_CITY_REQUIRED = "user.address.validation.city.required";
		public static final String USER_ADDRESS_VALIDATION_CITY_LENGTH = "user.address.validation.city.length";
		public static final String USER_ADDRESS_VALIDATION_STATE_REQUIRED = "user.address.validation.state.required";
		public static final String USER_ADDRESS_VALIDATION_STATE_LENGTH = "user.address.validation.state.length";
		public static final String USER_ADDRESS_VALIDATION_ZIPCODE_REQUIRED = "user.address.validation.zipcode.required";
		public static final String USER_ADDRESS_VALIDATION_ZIPCODE_FORMAT = "user.address.validation.zipcode.format";
	}

	public static class Events {
		private Events() {
		}

		public static final String EVENT_NOT_FOUND = "events.errors.event-not-found";
		public static final String EVENT_ACCESS_DENIED = "event.access.denied";
		public static final String INVALID_DISTANCE = "events.errors.invalid-distance";
		public static final String INVALID_DISTANCE_UNIT = "events.errors.invalid-distance-unit";

		public static final String EVENT_PUBLISH_ONLY_DRAFT = "event.publish.only-draft";
		public static final String EVENT_PUBLISH_PAST_DATE = "event.publish.past-date";
		public static final String EVENT_REGISTRATION_ONLY_PUBLISHED = "event.registration.only-published";
		public static final String EVENT_REGISTRATION_PERIOD_CLOSED = "event.registration.period-closed";
		public static final String EVENT_REGISTRATION_NOT_OPEN = "event.registration.not-open";
		public static final String EVENT_REGISTRATION_NOT_ACCEPTING = "event.registration.not-accepting";
		public static final String EVENT_BEGIN_MUST_HAVE_CLOSED_REGISTRATION = "event.begin.must-have-closed-registration";
		public static final String EVENT_COMPLETE_ONLY_IN_PROGRESS = "event.complete.only-in-progress";
		public static final String EVENT_CANCEL_INVALID_STATUS = "event.cancel.invalid-status";
		public static final String EVENT_PARTICIPANTS_NONE = "event.participants.none";
		public static final String EVENT_VALIDATION_NAME_REQUIRED = "event.validation.name.required";
		public static final String EVENT_VALIDATION_NAME_LENGTH = "event.validation.name.length";
		public static final String EVENT_VALIDATION_DATE_REQUIRED = "event.validation.date.required";
		public static final String EVENT_VALIDATION_DATE_FUTURE = "event.validation.date.future";
		public static final String EVENT_VALIDATION_LOCATION_REQUIRED = "event.validation.location.required";
		public static final String EVENT_VALIDATION_DISTANCE_REQUIRED = "event.validation.distance.required";
		public static final String EVENT_VALIDATION_PRICE_POSITIVE = "event.validation.price.positive";
		public static final String EVENT_VALIDATION_REGISTRATION_REQUIRED = "event.validation.registration.required";
		public static final String EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT = "event.validation.registration.before-event";
		public static final String EVENT_VALIDATION_MAX_PARTICIPANTS_POSITIVE = "event.validation.max-participants.positive";
		public static final String EVENT_VALIDATION_ORGANIZER_REQUIRED = "event.validation.organizer.required";
		
		public static final String EVENT_PROBLEM_DETAIL_NOT_FOUND = "event.problem.detail.not-found";
		public static final String EVENT_PROBLEM_DETAIL_NOT_PUBLISHABLE = "event.problem.detail.not-publishable";
		public static final String EVENT_PROBLEM_DETAIL_INVALID_STATUS = "event.problem.detail.invalid-status";
		public static final String EVENT_PROBLEM_DETAIL_REGISTRATION_FULL = "event.problem.detail.registration-full";
		public static final String EVENT_PROBLEM_DETAIL_REGISTRATION_CLOSED = "event.problem.detail.registration-closed";
		public static final String EVENT_PROBLEM_DETAIL_OPERATION_NOT_ALLOWED = "event.problem.detail.operation-not-allowed";
		public static final String EVENT_PROBLEM_DETAIL_VALIDATION_ERROR = "event.problem.detail.validation-error";
		public static final String EVENT_PROBLEM_DETAIL_ACCESS_DENIED = "event.problem.detail.access-denied";
		public static final String EVENT_VALIDATION_PLACE_REQUIRED = "event.validation.place.required";
		public static final String EVENT_VALIDATION_LATITUDE_INVALID = "event.validation.latitude.invalid";
		public static final String EVENT_VALIDATION_LONGITUDE_INVALID = "event.validation.longitude.invalid";
		public static final String EVENT_VALIDATION_REGISTRATION_PERIOD_START_NOT_NULL = "event.validation.registration-period.start.not-null";
		public static final String EVENT_VALIDATION_REGISTRATION_PERIOD_END_BEFORE_START = "event.validation.registration-period.end-before-start";
		public static final String EVENT_VALIDATION_ORGANIZER_NOT_FOUND = "event.validation.organizer.not-found";
		public static final String EVENT_VALIDATION_USER_NOT_ORGANIZER = "event.validation.user.not-organizer";
		
		public static final String EVENT_VALIDATION_DESCRIPTION_MAX_LENGTH = "event.validation.description.max-length";
		public static final String EVENT_VALIDATION_RACE_TYPE_REQUIRED = "event.validation.race-type.required";
		public static final String EVENT_VALIDATION_DISTANCE_POSITIVE = "event.validation.distance.positive";
		public static final String EVENT_VALIDATION_DISTANCE_MAX = "event.validation.distance.max";
		public static final String EVENT_VALIDATION_DISTANCE_UNIT_REQUIRED = "event.validation.distance-unit.required";
		public static final String EVENT_VALIDATION_PRICE_REQUIRED = "event.validation.price.required";
		public static final String EVENT_VALIDATION_REGISTRATION_START_REQUIRED = "event.validation.registration.start.required";
		public static final String EVENT_VALIDATION_REGISTRATION_START_FUTURE = "event.validation.registration.start.future";
		public static final String EVENT_VALIDATION_REGISTRATION_END_REQUIRED = "event.validation.registration.end.required";
		public static final String EVENT_VALIDATION_REGISTRATION_END_FUTURE = "event.validation.registration.end.future";
		public static final String EVENT_VALIDATION_MAX_PARTICIPANTS_MIN = "event.validation.max-participants.min";
		public static final String EVENT_VALIDATION_REGISTRATION_END_AFTER_START = "event.validation.registration.end-after-start";
	}

	public static class Images {
		private Images() {
		}

		public static final String INVALID_IMAGE_TYPE = "image.errors.invalid-type";
		public static final String INVALID_IMAGE_SIZE = "image.errors.invalid-size";
		public static final String UPLOAD_FAILED = "image.errors.upload-failed";
		public static final String IMAGE_NOT_FOUND = "image.errors.not-found";

		public static final String PROBLEM_INVALID_IMAGE = "image.problem.invalid-image";
		public static final String PROBLEM_UPLOAD_FAILED = "image.problem.upload-failed";
		public static final String PROBLEM_NOT_FOUND = "image.problem.not-found";
	}
}
