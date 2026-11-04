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

public class Card_17_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_60");
					put("warrior2", "17_60");

					put("armor", "17_41");      // Ceremonial Armor: [men] possession in the support area; offers to stack a [men] minion discarded in the maneuver phase (declined)
					put("brand", "17_47");      // Primitive Brand: [men] possession borne by a [men] minion; bearer is strength +2
					put("hides", "4_19");       // Hides: a [dunland] possession in the support area
					put("wait", "13_95");       // Lying in Wait: a [men] condition in the support area; a skirmish action, and a trigger that needs it borne
					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10; fierce only at a battleground (none on this path)
					put("squad", "11_99");      // Squad of Haradrim: [men] Man, strength 9; a regroup ability never used here
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man (Wizard), strength 8; +2 to [men] minions bearing a possession
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6 (+3 only at 2 burdens)
					put("stampede", "17_53");   // Stampeding Savage: Maneuver: discard him and X stacked minions to play a [men] minion from the discard pile

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Spot a [men] possession and discard this minion from play to make each [men] minion you spot strength +2 until the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Sunland Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityDiscardsHimToMakeEachMenMinionChosenStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var armor = scn.GetShadowCard("armor");
		var corps = scn.GetShadowCard("corps");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2, corps, saruman, axeman);
		scn.MoveCardsToSupportArea(armor);
		assertInZone(Zone.SUPPORT, armor);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);

		// Only this copy pays.  The Armor offers to stack the discarded Warrior before the bonus is chosen; declined.
		assertInZone(Zone.DISCARD, warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, warrior);

		// "Each [men] minion you spot": any of them, from none.  A [men] Man, a [men] Wizard and the other copy are
		// offered; the [raider] Man and the [men] possession are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, saruman, warrior2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, armor));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(corps, saruman);

		assertEquals(12, scn.GetStrength(corps));
		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMenPossessionBorneByAMinionIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var brand = scn.GetShadowCard("brand");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, corps);
		scn.AttachCardsTo(corps, brand);
		assertAttachedTo(brand, corps);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 10 + 2 for the Brand
		assertEquals(12, scn.GetStrength(corps));
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCards(corps);

		assertInZone(Zone.DISCARD, warrior);
		assertEquals(14, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoMenPossessionTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var hides = scn.GetShadowCard("hides");
		var wait = scn.GetShadowCard("wait");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, corps);
		// A possession of another culture, and a [men] card that is no possession.
		scn.MoveCardsToSupportArea(hides, wait);
		assertInZone(Zone.SUPPORT, hides, wait);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var armor = scn.GetShadowCard("armor");
		var corps = scn.GetShadowCard("corps");
		var squad = scn.GetShadowCard("squad");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.StartGame();
		scn.MoveMinionsToTable(warrior, corps, squad);
		scn.MoveCardsToSupportArea(armor);
		assertInZone(Zone.SUPPORT, armor);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(warrior);
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowChooseCards(corps, squad);
		assertEquals(12, scn.GetStrength(corps));
		assertEquals(11, scn.GetStrength(squad));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corps}, new PhysicalCardImpl[]{gimli, squad});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The second skirmish, after the first has resolved: still +2.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(11, scn.GetStrength(squad));
		assertEquals(12, scn.GetStrength(corps));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The start of the regroup phase: gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(9, scn.GetStrength(squad));
	}

	@Test
	public void AMenMinionPlayedAfterTheAbilityIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var brand = scn.GetShadowCard("brand");
		var corps = scn.GetShadowCard("corps");
		var squad = scn.GetShadowCard("squad");
		var stampede = scn.GetShadowCard("stampede");

		scn.MoveCardsToDiscard(squad);
		scn.StartGame();
		scn.MoveMinionsToTable(warrior, corps, stampede);
		scn.AttachCardsTo(corps, brand);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.DISCARD, squad);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCards(corps);
		// 10 + 2 for the Brand + 2
		assertEquals(14, scn.GetStrength(corps));
		assertEquals(8, scn.GetStrength(stampede));

		scn.FreepsPass();
		// The Stampeding Savage plays the Squad from the discard pile in the same maneuver phase.
		scn.ShadowUseCardAction(stampede);
		// The Warrior is in the discard pile too; the Squad is the one played.
		scn.ShadowChooseCardBPFromSelection(squad);
		assertInZone(Zone.SHADOW_CHARACTERS, squad);

		assertEquals(9, scn.GetStrength(squad));
		assertEquals(14, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
