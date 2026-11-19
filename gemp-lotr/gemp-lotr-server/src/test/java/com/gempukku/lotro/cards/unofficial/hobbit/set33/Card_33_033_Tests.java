package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_033_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("people", "33_33");
					put("people2", "33_33");

					// [elven] cards to spot: a character, an item and a card that is neither
					// Elrond, Elven Lord: [elven] Elf ally, Wise, 8/4; a start-of-turn heal (nothing is wounded) and a
					// regroup ability, not used
					put("elrond", "30_21");
					// Hadafang: [elven] artifact, hand weapon, on Elrond; a site-5 text and a fellowship ability, not used
					put("hadafang", "33_13");
					// Shoulder to Shoulder: [elven] support-area condition; a maneuver ability, not used
					put("shoulder", "1_59");
					// Galadriel, Elven Lady: [elven] Elf ally, Wise; a start-of-turn heal of Gandalf and a skirmish
					// ability, not used
					put("galadriel", "32_13");

					// Balin, Brother of Dwalin and Nori, Of the Blue Mountains: [dwarven] Dwarf companions, 7/3;
					// a fellowship ability and a maneuver ability, not used
					put("balin", "30_2");
					put("nori", "30_15");
					// Dwarven Axe: [dwarven] possession, hand weapon, on Balin; a mountain-site text, not reached
					put("axe", "31_1");
					// Dori, Really a decent fellow: [dwarven] follower, on Nori; an overwhelm bar
					put("dori", "30_8");
					// The Arkenstone: [dwarven] support-area artifact; Free Peoples abilities, not used
					put("arkenstone", "31_7");
					// Gandalf, The Grey: [gandalf] Wizard companion, bearing Glamdring: [gandalf] artifact, hand weapon
					put("gandalf", "30_27");
					put("glamdring", "30_29");

					// Orkish Aggressor: [gundabad] Orc, 6/2; an assignment ability, not used
					put("aggressor", "30_39");
					// Orkish Veteran: [sauron] Orc, 7/2; a regroup ability, not used
					put("veteran", "32_43");
					// Dark Bats: [moria] Creature, 4/2; a Shadow ability, not used
					put("bats", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SecretivePeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Secretive People
		 * Unique: False
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Remove (3) to make a minion strength +X (limit +3), where X is the number of [elven] cards you spot.<br><b>Regroup:</b> Exert a minion and remove (4) to discard a [dwarven] or [elven] card borne by a character. Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("people");

		assertEquals("Secretive People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityRemovesThreeToMakeAMinionStrengthPlusOneForEachElvenCardSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var elrond = scn.GetFreepsCard("elrond");
		var hadafang = scn.GetFreepsCard("hadafang");
		var shoulder = scn.GetFreepsCard("shoulder");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.SUPPORT, shoulder, people);
		scn.MoveMinionsToTable(aggressor, bats);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		// The Bats are left over and stay out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(people));
		scn.ShadowUseCardAction(people);
		assertEquals(2, scn.GetTwilight());
		// Three [elven] cards: Elrond, Hadafang and Shoulder to Shoulder. Bilbo, Balin and the minions are of other
		// cultures.
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(3, (int) scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("3");

		// Any minion, in the skirmish or not; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, bats));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, bilbo));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(6, scn.GetStrength(aggressor));
		scn.ShadowChooseCard(bats);
		assertEquals(7, scn.GetStrength(bats));
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneElvenCardTheMinionIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, people);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(people));
		scn.ShadowUseCardAction(people);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		// The Aggressor, the one minion, is chosen unasked.
		assertEquals(7, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoElvenCardTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, people);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.ShadowActionAvailable(people));
		scn.ShadowUseCardAction(people);
		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusIsLimitedToPlus3ForTheCardInEachSkirmishAndLastsTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var people2 = scn.GetShadowCard("people2");
		var elrond = scn.GetFreepsCard("elrond");
		var hadafang = scn.GetFreepsCard("hadafang");
		var shoulder = scn.GetFreepsCard("shoulder");
		var galadriel = scn.GetFreepsCard("galadriel");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(elrond, galadriel);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveCardsToSupportArea(people, people2);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.SUPPORT, shoulder, people, people2);
		scn.MoveMinionsToTable(aggressor, veteran, bats);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{nori, veteran});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(15);
		scn.FreepsPass();

		// First use: 2 of the 4 [elven] cards spotted, on the Aggressor.
		scn.ShadowUseCardAction(people);
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		scn.ShadowChooseCard(aggressor);
		assertEquals(12, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aggressor));

		// Second use, 2 again, on the Bats: the card has given +2 of its +3 this skirmish, so the Bats get +1.
		scn.FreepsPass();
		scn.ShadowUseCardAction(people);
		scn.ShadowChoose("2");
		scn.ShadowChooseCard(bats);
		assertEquals(9, scn.GetTwilight());
		assertEquals(5, scn.GetStrength(bats));
		assertEquals(8, scn.GetStrength(aggressor));

		// At the limit the ability is still offered; used, its cost is paid and nobody is stronger.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(people));
		scn.ShadowUseCardAction(people);
		scn.ShadowChoose("4");
		scn.ShadowChooseCard(aggressor);
		assertEquals(6, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aggressor));

		// The other copy has a limit of its own.
		scn.FreepsPass();
		scn.ShadowUseCardAction(people2);
		scn.ShadowChoose("2");
		scn.ShadowChooseCard(bats);
		assertEquals(3, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(bats));

		// The next skirmish is a phase of its own: the bonuses are gone, and the limit is new. Four [elven] cards
		// spotted give +3, the limit.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(nori);
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(7, scn.GetStrength(veteran));
		scn.FreepsPass();
		scn.ShadowUseCardAction(people);
		assertEquals(0, scn.GetTwilight());
		scn.ShadowChoose("4");
		scn.ShadowChooseCard(veteran);
		assertEquals(10, scn.GetStrength(veteran));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, people);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(people));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, people);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(people));
	}

	@Test
	public void RegroupAbilityExertsAMinionAndRemovesFourToDiscardADwarvenOrElvenCardBorneByACharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var people2 = scn.GetShadowCard("people2");
		var elrond = scn.GetFreepsCard("elrond");
		var hadafang = scn.GetFreepsCard("hadafang");
		var shoulder = scn.GetFreepsCard("shoulder");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var nori = scn.GetFreepsCard("nori");
		var dori = scn.GetFreepsCard("dori");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCardsToSupportArea(shoulder, arkenstone);
		scn.MoveCompanionsToTable(balin, nori, gandalf);
		scn.AttachCardsTo(balin, axe);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToSupportArea(people, people2);

		scn.StartGame();
		scn.AttachCardsTo(nori, dori);
		scn.MoveMinionsToTable(aggressor, bats);
		assertAttachedTo(hadafang, elrond);
		assertAttachedTo(axe, balin);
		assertAttachedTo(dori, nori);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, shoulder, arkenstone, people, people2);

		// The minions are left unassigned and stand in the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(people));
		scn.ShadowUseCardAction(people);
		// The exertion: minions of two cultures and races; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, bats));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.ShadowChooseCard(aggressor);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertEquals(2, scn.GetTwilight());

		// [dwarven] and [elven] cards borne by a companion and an ally, a follower among them; not those in the
		// support area, nor a borne card of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hadafang, axe, dori));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shoulder, arkenstone, glamdring));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hadafang);
		assertInDiscard(hadafang);
		assertAttachedTo(axe, balin);
		assertAttachedTo(dori, nori);

		// Then this condition is discarded; the other copy stays.
		assertInDiscard(people);
		assertInZone(Zone.SUPPORT, people2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoDwarvenOrElvenCardBorneTheRegroupAbilityIsPaidForNothingAndStillDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var people2 = scn.GetShadowCard("people2");
		var shoulder = scn.GetFreepsCard("shoulder");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(shoulder, arkenstone);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToSupportArea(people, people2);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, shoulder, arkenstone, people, people2);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(people));
		scn.ShadowUseCardAction(people);
		// The Aggressor, the one minion, exerts unasked.
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, shoulder, arkenstone);
		assertAttachedTo(glamdring, gandalf);
		assertInDiscard(people);
		assertInZone(Zone.SUPPORT, people2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, axe);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		assertAttachedTo(axe, balin);
		assertInZone(Zone.SUPPORT, people);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(people));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithTheOneMinionExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var people = scn.GetShadowCard("people");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, axe);
		scn.MoveCardsToSupportArea(people);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		// Vitality 2 with 1 wound: exhausted. Balin, able to exert, is no minion.
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertAttachedTo(axe, balin);
		assertInZone(Zone.SUPPORT, people);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(people));
	}
}
