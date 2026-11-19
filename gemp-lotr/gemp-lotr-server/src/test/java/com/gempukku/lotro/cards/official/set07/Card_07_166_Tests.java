package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_166_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leader", "7_166");
					put("leader2", "7_166");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card; a regroup ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion of vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a second [shire] companion; his abilities are used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally; a fellowship ability only
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] ally; a maneuver ability only
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition; a skirmish modifier only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronLeaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Leader
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * <b>Archery:</b> Spot 4 Free Peoples cultures and either exert this minion or remove a threat to wound a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");

		assertEquals("Southron Leader", leader.getBlueprint().getTitle());
		assertNull(leader.getBlueprint().getSubtitle());
		assertFalse(leader.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, leader.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, leader.getBlueprint().getCulture());
		assertEquals(CardType.MINION, leader.getBlueprint().getCardType());
		assertEquals(Race.MAN, leader.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(leader, Keyword.SOUTHRON));
		assertEquals(5, leader.getBlueprint().getTwilightCost());
		assertEquals(10, leader.getBlueprint().getStrength());
		assertEquals(3, leader.getBlueprint().getVitality());
		assertEquals(4, leader.getBlueprint().getSiteNumber());
	}

		@Test
	public void WithBothCostsPayableItAsksWhichAndForAThreatWoundsAChosenCompanionOtherThanTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);

		var leader = scn.GetShadowCard("leader");
		var leader2 = scn.GetShadowCard("leader2");

		scn.StartGame();
		scn.MoveMinionsToTable(leader, leader2);
		scn.AddThreats(1);
		// Vitality 3 with 2 wounds: exhausted, so a wound kills him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Exactly 4: [shire] Frodo and Sam, [gondor] Aragorn, [dwarven] Gimli, and [gandalf] by an ally alone.
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(leader2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.ShadowActionAvailable(leader));

		scn.ShadowUseCardAction(leader);
		// Both halves of the cost can be paid: the Shadow player is asked which (Q-0308).
		assertEquals(List.of("Exert this minion", "Remove a threat"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove a threat");
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(leader2));

		// Companions of three cultures and races, an exhausted one among them; not the Ring-bearer, not an ally,
		// not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman, leader, leader2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// A wound, not an exertion: the exhausted Dwarf is killed.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoThreatThisCopyExertsUnaskedToWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);

		var leader = scn.GetShadowCard("leader");
		var leader2 = scn.GetShadowCard("leader2");

		scn.StartGame();
		scn.MoveMinionsToTable(leader, leader2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(leader2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(leader));

		scn.ShadowUseCardAction(leader);
		// No threat to remove: the exertion is the only half that can be paid, and is paid by the copy used.
		assertFalse(scn.ShadowDecisionAvailable("Remove a threat"));
		assertEquals(1, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(leader2));

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithThisCopyExhaustedTheThreatIsRemovedUnaskedToWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);

		var leader = scn.GetShadowCard("leader");

		scn.StartGame();
		scn.MoveMinionsToTable(leader);
		scn.AddThreats(1);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(leader, 2);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(leader));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(leader));

		scn.ShadowUseCardAction(leader);
		assertFalse(scn.ShadowDecisionAvailable("Exert this minion"));
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(leader));

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman));
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithThreeCulturesBesideAGollumCardAndASecondShireCompanionItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		var leader = scn.GetShadowCard("leader");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(leader, gollum);
		// Both halves of the cost could be paid.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// [shire] twice, [gondor], [dwarven]: 3; the [gollum] Shadow card is no Free Peoples culture.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(leader));
		assertFalse(scn.ShadowActionAvailable(leader));
	}

	@Test
	public void WithThisCopyExhaustedAndNoThreatItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);

		var leader = scn.GetShadowCard("leader");
		var leader2 = scn.GetShadowCard("leader2");

		scn.StartGame();
		scn.MoveMinionsToTable(leader, leader2);
		scn.AddWoundsToChar(leader, 2);
		assertEquals(2, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(leader2));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(leader));
		// Neither half can be paid for the exhausted copy; the other copy can still exert.
		assertFalse(scn.ShadowActionAvailable(leader));
		assertTrue(scn.ShadowActionAvailable(leader2));
	}

	@Test
	public void WithTheRingBearerTheOnlyCompanionItIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barliman = scn.GetFreepsCard("barliman");
		var merchant = scn.GetFreepsCard("merchant");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveAlliesToTable(barliman, merchant);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.SUPPORT, stairs);

		var leader = scn.GetShadowCard("leader");

		scn.StartGame();
		scn.MoveMinionsToTable(leader);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// [shire] Frodo, [gandalf] and [gondor] by allies, [dwarven] by a condition: 4.
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.ShadowActionAvailable(leader));

		scn.ShadowUseCardAction(leader);
		// The exertion is paid, and no companion but the Ring-bearer is there to wound.
		assertEquals(1, scn.GetWoundsOn(leader));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
