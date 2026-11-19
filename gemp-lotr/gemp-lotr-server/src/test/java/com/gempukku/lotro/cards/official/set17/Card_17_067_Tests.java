package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "17_67");

					// Denizen of Khazad-dum: an [orc] Orc, strength 9, vitality 2; +2 at an underground site only
					put("denizen", "11_115");
					put("denizen2", "11_115");
					// Orkish Cavalry: an [orc] Orc, strength 8, vitality 2; a draw when an [orc] mount is played, and an
					// Assignment ability that is not used
					put("cavalry", "17_74");
					// Vicious Warg: an [orc] mount for an [orc] Orc of strength 9 or less; bearer is fierce
					put("vicious", "17_92");
					// Threatening Warg: an [orc] mount for an [orc] Orc of strength 10 or less; bearer is fierce; its play
					// trigger is not reached (cheated on)
					put("threatening", "17_91");
					// Rider's Gear: [orc] armor for an [orc] Orc, a possession that is no mount; cannot take wounds
					// outside skirmishes only while mounted
					put("gear", "17_90");
					// Mordor Brute: a [sauron] Orc, vitality 3; a [sauron] Orc bearing a possession takes no wounds
					// outside skirmishes
					put("brute", "10_90");
					// Gothmog's Warg: a [sauron] mount for a [sauron] Orc; strength +1 for each site its owner controls
					put("gothmogswarg", "10_89");

					// Aragorn, Ranger of the North: vitality 4; his Maneuver ability is not used
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: vitality 3; his Skirmish ability is not used
					put("gimli", "1_13");
					// Boromir, Son of Denethor: vitality 3; his Skirmish ability is not used
					put("boromir", "1_97");
					// Thrarin, Dwarven Smith: a Dwarf ally.  "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ADefiledChargeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: A Defiled Charge
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, exert your mounted [orc] Orc.<br>Each time the Free Peoples player assigns a mounted [orc] Orc to a companion, he or she must choose to either exert that companion or add (2).
		*/

		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");

		assertEquals("A Defiled Charge", charge.getBlueprint().getTitle());
		assertNull(charge.getBlueprint().getSubtitle());
		assertFalse(charge.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, charge.getBlueprint().getSide());
		assertEquals(Culture.ORC, charge.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, charge.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(charge, Keyword.SUPPORT_AREA));
		assertEquals(2, charge.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertYourMountedOrcOrcAndNoOtherOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var cavalry = scn.GetShadowCard("cavalry");
		var vicious = scn.GetShadowCard("vicious");
		var threatening = scn.GetShadowCard("threatening");
		var gear = scn.GetShadowCard("gear");
		var brute = scn.GetShadowCard("brute");
		var gothmogswarg = scn.GetShadowCard("gothmogswarg");
		scn.MoveCardsToHand(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, denizen2, cavalry, brute);
		scn.AttachCardsTo(denizen, vicious);
		scn.AttachCardsTo(cavalry, threatening);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(brute, gothmogswarg);
		assertAttachedTo(vicious, denizen);
		assertAttachedTo(threatening, cavalry);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(gothmogswarg, brute);
		assertInZone(Zone.HAND, charge);
		assertEquals(0, scn.GetWoundsOn(cavalry));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(denizen2));
		assertEquals(0, scn.GetWoundsOn(brute));
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(charge));
		scn.ShadowPlayCard(charge);
		// Two mounted [orc] Orcs on different mounts; not an [orc] Orc bearing a possession that is no mount, nor a
		// mounted [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, cavalry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(denizen2, brute));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cavalry);

		assertEquals(1, scn.GetWoundsOn(cavalry));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(denizen2));
		assertEquals(0, scn.GetWoundsOn(brute));
		assertInZone(Zone.SUPPORT, charge);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTheOnlyMountedOrcOrcExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var vicious = scn.GetShadowCard("vicious");
		var gear = scn.GetShadowCard("gear");
		var brute = scn.GetShadowCard("brute");
		var gothmogswarg = scn.GetShadowCard("gothmogswarg");
		scn.MoveCardsToHand(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, denizen2, brute);
		scn.AttachCardsTo(denizen, vicious);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(brute, gothmogswarg);
		// Vitality 2 and the Warg's +2, with 3 wounds: exhausted.  The other two Orcs could exert.
		scn.AddWoundsToChar(denizen, 3);
		assertEquals(3, scn.GetWoundsOn(denizen));
		assertAttachedTo(vicious, denizen);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(gothmogswarg, brute);
		assertInZone(Zone.HAND, charge);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(charge));
	}

	@Test
	public void AMountedOrcOrcTheFreePeoplesPlayerAssignsToACompanionMakesHimChooseToExertThatCompanionOrAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var vicious = scn.GetShadowCard("vicious");
		var gear = scn.GetShadowCard("gear");
		var brute = scn.GetShadowCard("brute");
		var gothmogswarg = scn.GetShadowCard("gothmogswarg");
		scn.MoveCardsToSupportArea(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, denizen2, brute);
		scn.AttachCardsTo(denizen, vicious);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(brute, gothmogswarg);
		assertInZone(Zone.SUPPORT, charge);
		assertAttachedTo(vicious, denizen);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(gothmogswarg, brute);
		scn.SkipToAssignments();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The mounted [orc] Orc to Aragorn, the [orc] Orc bearing armor to Boromir, the mounted [sauron] Orc to Frodo.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, denizen},
				new PhysicalCardImpl[]{boromir, denizen2},
				new PhysicalCardImpl[]{frodo, brute});

		// One choice, his, naming the companion assigned.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Aragorn"));
		assertTrue(scn.FreepsChoiceAvailable("Add 2"));
		scn.FreepsChoose("Exert");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TwoMountedOrcOrcsAssignedInOneAnswerAskTwiceAndAdd2Each() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var cavalry = scn.GetShadowCard("cavalry");
		var vicious = scn.GetShadowCard("vicious");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, cavalry);
		scn.AttachCardsTo(denizen, vicious);
		scn.AttachCardsTo(cavalry, threatening);
		assertInZone(Zone.SUPPORT, charge);
		assertAttachedTo(vicious, denizen);
		assertAttachedTo(threatening, cavalry);
		scn.SkipToAssignments();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, cavalry});

		assertTrue(scn.FreepsChoiceAvailable("Add 2"));
		scn.FreepsChoose("Add 2");
		assertEquals(twilight + 2, scn.GetTwilight());
		// A second time, for the other assignment.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Add 2"));
		scn.FreepsChoose("Add 2");

		assertEquals(twilight + 4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithThatCompanionExhaustedTwoAreAddedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToSupportArea(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, vicious);
		// Vitality 4 with 3 wounds: exhausted, so "exert that companion" cannot be done.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, charge);
		assertAttachedTo(vicious, denizen);
		scn.SkipToAssignments();

		int twilight = scn.GetTwilight();
		scn.FreepsAssignToMinions(aragorn, denizen);

		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayersAssignmentOfAMountedOrcOrcAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToSupportArea(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, vicious);
		assertInZone(Zone.SUPPORT, charge);
		assertAttachedTo(vicious, denizen);
		scn.SkipToAssignments();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The Free Peoples player leaves the Orc over; the Shadow player assigns it to Aragorn.
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(aragorn, denizen);

		assertTrue(scn.IsCharAssigned(aragorn));
		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnAllyAssignedToAMountedOrcOrcAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var charge = scn.GetShadowCard("charge");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToSupportArea(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, vicious);
		assertInZone(Zone.SUPPORT, charge);
		assertAttachedTo(vicious, denizen);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		int twilight = scn.GetTwilight();
		scn.FreepsAssignToMinions(thrarin, denizen);

		assertTrue(scn.IsCharAssigned(thrarin));
		assertFalse(scn.FreepsChoiceAvailable("Exert"));
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
