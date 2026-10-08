package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_051_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "55_51");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] companion
					put("guard", "1_7");        // Dwarf Guard: unbound [dwarven] companion, strength 4, vitality 2
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] companion, a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire], Ring-bound by rule, not the Ring-bearer
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] ally, the fourth Free Peoples culture

					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] condition on Aragorn, strength -1, no other text
					put("saga", "1_114");       // The Saga of Elendil: [gondor] tale on a [gondor] companion; skirmish ability used only on purpose
					put("endurance", "2_4");    // Endurance of Dwarves: [dwarven] condition on a Dwarf, vitality +1; skirmish ability used only on purpose
					put("endurance2", "2_4");
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession on a Dwarf, no number
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition on a companion: at a sanctuary bearer cannot heal
					put("evil2", "1_283");
					put("weary", "4_212");      // Weary: [isengard] condition on a companion or ally; exerts bearer only when assigned to an [isengard] tracker
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support-area condition; does nothing without an Uruk-hai
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Grima, Chief Counselor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Maneuver: Exert Grima to hinder a condition borne by an unbound companion (or hinder a companion and all Free Peoples cards they bear if you can spot 4 Free Peoples cultures).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Grima", card.getBlueprint().getTitle());
		assertEquals("Chief Counselor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysForTwoPlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 for Frodo + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(grima));
		scn.ShadowPlayCard(grima);
		// 2 to play + 2 roaming (site 3, played at site 2)
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, grima);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeCulturesExertsToHinderAConditionBorneByAnUnboundCompanionAndOnlyInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		var legacy = scn.GetShadowCard("legacy");
		var saga = scn.GetFreepsCard("saga");
		var endurance = scn.GetFreepsCard("endurance");
		var armor = scn.GetFreepsCard("armor");
		var evil = scn.GetShadowCard("evil");
		var evil2 = scn.GetShadowCard("evil2");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, legacy, saga);
		scn.AttachCardsTo(guard, endurance, armor, evil);
		scn.AttachCardsTo(sam, evil2);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.MoveCardsToSupportArea(armory);
		// Already hindered: Aragorn's tale, and Elrond, whose [elven] culture cannot then be spotted.
		scn.HinderCard(saga, elrond);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// [gondor], [dwarven] and [shire]: three Free Peoples cultures. The Shadow cards on the companions are no Free Peoples culture.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		// Conditions of both sides on two unbound companions; not the condition on Sam (Ring-bound), nor a possession,
		// nor a condition in a support area, nor one already hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legacy, endurance, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(evil2, armor, armory, saga, aragorn, guard, sam, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(endurance);
		assertTrue(scn.IsHindered(endurance));
		assertFalse(scn.IsHindered(legacy));
		assertFalse(scn.IsHindered(evil));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(guard));
		assertFalse(scn.IsHindered(evil2));
		assertFalse(scn.IsHindered(armory));

		// Not in the archery phase, the next in which the Shadow player is asked for actions.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(grima));
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima));
	}

	@Test
	public void AConditionBorneByAHinderedUnboundCompanionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var legacy = scn.GetShadowCard("legacy");
		var endurance = scn.GetFreepsCard("endurance");
		var endurance2 = scn.GetFreepsCard("endurance2");
		scn.MoveCompanionsToTable(aragorn, guard, gimli);
		scn.AttachCardsTo(aragorn, legacy);
		scn.AttachCardsTo(guard, endurance);
		scn.AttachCardsTo(gimli, endurance2);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.HinderCard(gimli);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(3, scn.GetFreepsCultureCount());
		scn.ShadowUseCardAction(grima);

		assertTrue(scn.ShadowHasCardChoiceAvailable(legacy, endurance));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(endurance2));
		scn.ShadowChooseCard(legacy);
		assertTrue(scn.IsHindered(legacy));
		assertFalse(scn.IsHindered(endurance));
		assertFalse(scn.IsHindered(endurance2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFourCulturesHindersAnUnboundCompanionAndTheFreePeoplesCardsHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var legacy = scn.GetShadowCard("legacy");
		var endurance = scn.GetFreepsCard("endurance");
		var armor = scn.GetFreepsCard("armor");
		var evil = scn.GetShadowCard("evil");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, legacy, saga);
		scn.AttachCardsTo(guard, endurance, armor, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// [gondor], [dwarven], [shire] and Elrond's [elven].
		assertEquals(4, scn.GetFreepsCultureCount());
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));

		// The bracket is taken when it can be: a companion is chosen, not a condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, grima, legacy, saga, endurance, evil));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(guard);
		// He and the Free Peoples cards he bears; not the Shadow condition on him, and nothing of Aragorn's (his
		// tale is a Free Peoples card borne by a companion who was not chosen).
		assertTrue(scn.IsHindered(guard));
		assertTrue(scn.IsHindered(endurance));
		assertTrue(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(evil));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(legacy));
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(elrond));
		assertInZone(Zone.ATTACHED, endurance, armor, evil, saga);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	@Ignore("F-0239: the bracket's \"a companion\" is built as choose(unbound,companion)")
	public void WithFourCulturesARingBoundCompanionAndTheRingBearerMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		var evil = scn.GetShadowCard("evil");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(sam, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(4, scn.GetFreepsCultureCount());
		scn.ShadowUseCardAction(grima);

		// "A companion": the Ring-bound Sam and the Ring-bearer as well as the unbound.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard, sam, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond));
		scn.ShadowChooseCard(sam);
		assertTrue(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(evil));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OfferedWithNothingToHinderUntilGrimaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var evil = scn.GetShadowCard("evil");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(elrond);
		// Conditions on a Ring-bound companion and on an ally only, and three Free Peoples cultures.
		scn.AttachCardsTo(sam, evil);
		scn.AttachCardsTo(elrond, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(3, scn.GetFreepsCultureCount());

		// The exertion is a cost: the ability is offered, and spends it for nothing.
		assertTrue(scn.ShadowActionAvailable(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertFalse(scn.IsHindered(evil));
		assertFalse(scn.IsHindered(weary));
		assertFalse(scn.IsHindered(sam));

		scn.FreepsPass();
		scn.ShadowUseCardAction(grima);
		assertEquals(2, scn.GetWoundsOn(grima));

		// Vitality 3 with 2 wounds: exhausted, and no longer offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima));
	}
}
