package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speed", "15_180");
					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; hunter 1 is its whole text
					put("tracking2", "15_171");
					put("butcher", "17_122");    // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2; hunter 6 is its whole text
					put("whu", "18_131");        // White Hand Uruk: [uruk-hai] Uruk-hai, strength 10, vitality 2, no hunter; damage +1, and a play requirement (cheated in)
					put("herdsman", "15_83");    // Hunting Herdsman: a [men] Man, hunter 1, strength 8; +3 for each follower, and none is laid
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("baruk", "5_5");         // Baruk Khazad: Maneuver: exert a Dwarf to wound a minion
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] Dwarf companion, no hunter; his ability is not used
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, no hunter; his ability is not used
					put("boromir", "1_97");      // Boromir, Son of Denethor: [gondor] Man companion, vitality 3, no hunter; his ability is not used
					put("faramir", "18_48");     // Faramir, Captain of Ithilien: [gondor] Man companion, strength 7, hunter 2; his ability is not used
					put("thrarin", "1_27");      // Thrarin, Dwarven Smith: a Dwarf ALLY, no hunter; his ability is not used
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("longifell", "4_97");    // Long I Fell: Skirmish: spot Gandalf to prevent all wounds to him; the Shadow player may make you wound a minion to prevent this
					put("legolas", "1_50");      // Legolas, Greenleaf: an archer, for a Free Peoples archery wound; his ability is not used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSarumanScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speed", "15_180");
					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 is its whole text
					put("cirith", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 is its whole text
					put("saruman", "17_37");     // Saruman, Instigator of Insurrection: [isengard] Wizard; while 6 companions can be spotted, each minion gains hunter 2
					put("knight", "5_35");       // Gondorian Knight: [gondor] Man, knight and no other text
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("knight4", "5_35");
					put("knight5", "5_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithAllPossibleSpeedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: With All Possible Speed
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each unwounded hunter [uruk-hai] minion is strength +1.<br><b>Response:</b> If your hunter minion is about to take a wound during a skirmish, discard that minion to exert a non-hunter companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("speed");

		assertEquals("With All Possible Speed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUnwoundedHunterUrukHaiCultureMinionIsStrengthPlus1UntilItIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var tracking = scn.GetShadowCard("tracking");
		var tracking2 = scn.GetShadowCard("tracking2");
		var butcher = scn.GetShadowCard("butcher");
		var whu = scn.GetShadowCard("whu");
		var herdsman = scn.GetShadowCard("herdsman");
		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, tracking2, butcher, whu, herdsman);
		scn.MoveCardsToSupportArea(speed);
		scn.AddWoundsToChar(tracking2, 1);
		assertEquals(1, scn.GetWoundsOn(tracking2));
		scn.SkipToPhase(Phase.MANEUVER);

		// Two unwounded [uruk-hai] hunters, +1 each.
		assertEquals(15, scn.GetStrength(tracking));
		assertEquals(6, scn.GetStrength(butcher));
		// A wounded one, one that is no hunter, and a hunter of another culture: as printed.
		assertEquals(14, scn.GetStrength(tracking2));
		assertEquals(10, scn.GetStrength(whu));
		assertEquals(8, scn.GetStrength(herdsman));

		// Gimli, the one Dwarf, exerts to wound the first Tracking Uruk.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(tracking));
		scn.FreepsPlayCard(baruk);
		scn.FreepsChooseCard(tracking);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(tracking));

		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(6, scn.GetStrength(butcher));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureWhoHasGainedHunterIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSarumanScenario();

		var speed = scn.GetShadowCard("speed");
		var tracking = scn.GetShadowCard("tracking");
		var cirith = scn.GetShadowCard("cirith");
		var saruman = scn.GetShadowCard("saruman");
		// With the Ring-bearer, six companions.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("knight"), scn.GetFreepsCard("knight2"), scn.GetFreepsCard("knight3"),
				scn.GetFreepsCard("knight4"), scn.GetFreepsCard("knight5"));

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, cirith, saruman);
		scn.MoveCardsToSupportArea(speed);
		scn.FreepsRefreshDecision();

		// Saruman makes every minion a hunter: the [sauron] Uruk-hai and Saruman himself, an [isengard] Wizard.
		assertEquals(2, scn.GetKeywordCount(cirith, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(saruman, Keyword.HUNTER));
		assertEquals(9, scn.GetStrength(cirith));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(15, scn.GetStrength(tracking));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHunterMinionAboutToTakeAWoundForLosingIsDiscardedToExertANonHunterCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var tracking = scn.GetShadowCard("tracking");
		var whu = scn.GetShadowCard("whu");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, faramir);
		scn.MoveAlliesToTable(thrarin);
		// Boromir is exhausted.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(tracking, whu);
		scn.MoveCardsToSupportArea(speed);
		// A strength that only arranges the board: Aragorn beats both minions, 8 + 19 against 16 + 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 19));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		// The Shadow player puts the White Hand Uruk beside the Tracking Uruk.
		scn.ShadowAssignToMinions(aragorn, whu);
		scn.FreepsResolveSkirmish(aragorn);
		// 14 + 1 for hunter + 1 for this card.
		assertEquals(16, scn.GetStrength(tracking));
		assertEquals(27, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Both minions lose; only the hunter's wound is answered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(speed));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, tracking);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(faramir, thrarin, boromir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The other loser takes its own wound, and nothing more is offered.
		assertEquals(1, scn.GetWoundsOn(whu));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHunterMinionOfAnotherCultureAndRaceAboutToTakeAWoundForLosingIsDiscardedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var herdsman = scn.GetShadowCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman);
		scn.MoveCardsToSupportArea(speed);
		// A strength that only arranges the board: Aragorn 13 beats the Herdsman's 8 + 1 for hunter, short of double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, herdsman);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// The text says "hunter minion", whatever its culture or race: a [men] Man hunter is answered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(speed));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, herdsman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, scn.GetRingBearer()));
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnArcheryWoundOnAHunterMinionIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var tracking = scn.GetShadowCard("tracking");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.MoveCardsToSupportArea(speed);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(tracking));
		scn.FreepsPass();
		scn.ShadowPass();

		// The fellowship archery total of 1 goes on the one minion, outside any skirmish: no response.
		assertEquals(1, scn.GetWoundsOn(tracking));
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(speed));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AHunterMinionWaitingForItsSkirmishWoundedDuringAnotherIsDiscardedToExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var tracking = scn.GetShadowCard("tracking");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var longifell = scn.GetFreepsCard("longifell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(longifell);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, troop);
		scn.MoveCardsToSupportArea(speed);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop},
				new PhysicalCardImpl[]{aragorn, tracking});
		scn.FreepsResolveSkirmish(gandalf);

		// The Tracking Uruk waits for its skirmish with Aragorn.
		assertTrue(scn.IsCharAssigned(tracking));
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(longifell);
		// The Shadow player has the Free Peoples player wound a minion, to prevent the spell.
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(tracking);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(speed));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, tracking);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AHunterCompanionLosingASkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var troop = scn.GetShadowCard("troop");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(speed);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);
		// 13 against 7 + 2 for hunter: Faramir loses, and is not overwhelmed.
		assertEquals(9, scn.GetStrength(faramir));
		assertEquals(0, scn.GetWoundsOn(faramir));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(speed));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoNonHunterCompanionAbleToExertTheMinionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speed = scn.GetShadowCard("speed");
		var butcher = scn.GetShadowCard("butcher");
		var faramir = scn.GetFreepsCard("faramir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir);
		// The Ring-bearer, the one companion who is no hunter, is exhausted.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(butcher);
		scn.MoveCardsToSupportArea(speed);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, butcher);
		scn.FreepsResolveSkirmish(faramir);
		// Two hunters: neither has a hunter bonus. 7 against 5 + 1 for this card.
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(6, scn.GetStrength(butcher));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(speed));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, butcher);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
