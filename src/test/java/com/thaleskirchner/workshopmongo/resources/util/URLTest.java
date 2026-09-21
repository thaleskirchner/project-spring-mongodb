package com.thaleskirchner.workshopmongo.resources.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link URL}: the small helper the resource layer uses to
 * decode query-string parameters and parse the {@code yyyy-MM-dd} date
 * filters accepted by {@code PostResource#fullSearch}.
 */
class URLTest {

	@Test
	void decodeParamDecodesUrlEncodedText() {
		String decoded = URL.decodeParam("Bom%20dia");

		assertThat(decoded).isEqualTo("Bom dia");
	}

	@Test
	void decodeParamReturnsSameTextWhenNotEncoded() {
		String decoded = URL.decodeParam("viagem");

		assertThat(decoded).isEqualTo("viagem");
	}

	@Test
	void convertDateParsesValidIsoDate() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date expected = sdf.parse("2026-03-21");

		Date converted = URL.convertDate("2026-03-21", new Date(0L));

		assertThat(converted).isEqualTo(expected);
	}

	@Test
	void convertDateReturnsDefaultValueWhenTextIsBlank() {
		Date defaultValue = new Date(0L);

		Date converted = URL.convertDate("", defaultValue);

		assertThat(converted).isEqualTo(defaultValue);
	}

	@Test
	void convertDateReturnsDefaultValueWhenTextIsInvalid() {
		Date defaultValue = new Date(0L);

		Date converted = URL.convertDate("not-a-date", defaultValue);

		assertThat(converted).isEqualTo(defaultValue);
	}
}
