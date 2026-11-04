package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elendil", "13_65");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit knight; "To play, spot a [gondor] companion"; a skirmish ability only
					put("faramir", "12_47");    // Faramir, Dúnadan of Gondor: [gondor] Man, a ranger and no knight; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, no knight; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally, a Man of vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil: [gondor] artifact on a [gondor] Man, vitality +1 (so that the Merchant can exert)
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and no other text
					put("scout", "11_18");      // Elven Scout: [elven] Elf companion, resistance 7; muster only beside another Elf
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					// Garrison of Osgiliath and Heirs of Gondor: [gondor] support-area conditions that take [gondor]
					// tokens as they are played; skirmish abilities only
					put("garrison", "6_52");
					put("heirs", "13_69");
					// Wish For Our Kinfolk: [dwarven] support-area condition that adds [dwarven] tokens to itself
					put("wish", "8_8");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElendilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Elendil, High-king of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 5
		 * Resistance: 6
		 * Game Text: <b>Knight</b>. To play, spot 2 [gondor] knights or exert 2 [gondor] Men.<br>Each of your [gondor] companions is resistance +1.<br><b>Maneuver:</b> Remove a [gondor] token to make an unbound companion resistance +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");

		assertEquals("Elendil", elendil.getBlueprint().getTitle());
		assertEquals("High-king of Gondor", elendil.getBlueprint().getSubtitle());
		assertTrue(elendil.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, elendil.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, elendil.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, elendil.getBlueprint().getCardType());
		assertEquals(Race.MAN, elendil.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(elendil, Keyword.KNIGHT));
		assertEquals(5, elendil.getBlueprint().getTwilightCost());
		assertEquals(8, elendil.getBlueprint().getStrength());
		assertEquals(5, elendil.getBlueprint().getVitality());
		assertEquals(6, elendil.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilTwoGondorKnightsAreSpottedAndIsThenPlayedWithNothingExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(knight, faramir);
		// Faramir, a [gondor] Man who is no knight, is exhausted: the Knight is the only [gondor] Man able to exert.
		scn.AddWoundsToChar(faramir, 2);
		scn.MoveCardsToHand(elendil, pippin);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(faramir));
		// One knight, and one [gondor] Man able to exert.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(elendil));

		// Pippin is a second [gondor] knight, and no Man.
		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsPlayAvailable(elendil));
		scn.FreepsPlayCard(elendil);
		assertInPlay(elendil);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTwoKnightsItIsPlayedByExertingTwoGondorMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var pippin = scn.GetFreepsCard("pippin");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(pippin, faramir, boromir, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(elendil);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		assertEquals(2, scn.GetVitality(merchant));
		assertTrue(scn.FreepsPlayAvailable(elendil));
		// Pippin is the only knight, so only the exertion can be paid, and nothing is asked between the two.
		scn.FreepsPlayCard(elendil);
		// [gondor] Men, companions and the ally; not the [rohan] Man nor the [gondor] Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, pippin));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(faramir, merchant);
		assertInPlay(elendil);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBothAlternativesPayableThePlayerIsAskedWhichAndMayExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(knight, pippin, faramir, boromir);
		scn.MoveCardsToHand(elendil);

		scn.StartGame();
		scn.FreepsPlayCard(elendil);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("knights"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2"));
		scn.FreepsChooseOption("Exert 2");
		scn.FreepsChooseCards(faramir, boromir);
		assertInPlay(elendil);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachOfYourGondorCompanionsIsResistancePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		assertEquals(6, scn.GetResistance(knight));
		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(0, scn.GetResistance(merchant));
		assertEquals(10, scn.GetResistance(frodo));

		scn.MoveCompanionsToTable(elendil);
		// Elendil himself, a [gondor] Man and a [gondor] Hobbit; not the [rohan] companion, the [gondor] ally or
		// Frodo.
		assertEquals(7, scn.GetResistance(elendil));
		assertEquals(7, scn.GetResistance(knight));
		assertEquals(7, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(0, scn.GetResistance(merchant));
		assertEquals(10, scn.GetResistance(frodo));
	}

	@Test
	public void ManeuverAbilityRemovesAGondorTokenToMakeAnUnboundCompanionResistancePlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var knight = scn.GetFreepsCard("knight");
		var scout = scn.GetFreepsCard("scout");
		var sam = scn.GetFreepsCard("sam");
		var merchant = scn.GetFreepsCard("merchant");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var wish = scn.GetFreepsCard("wish");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(elendil, knight, scout, sam);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(garrison, heirs, wish);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(wish, 1);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetResistance(scout));
		assertEquals(7, scn.GetResistance(knight));
		assertTrue(scn.FreepsActionAvailable(elendil));
		scn.FreepsUseCardAction(elendil);
		// A token from either [gondor] card; never the [dwarven] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wish));
		scn.FreepsChooseCard(garrison);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		// Unbound companions of two cultures, Elendil among them; not Sam (Ring-bound), Frodo or the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elendil, knight, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, merchant));
		scn.FreepsChooseCard(scout);
		assertEquals(8, scn.GetResistance(scout));
		assertEquals(7, scn.GetResistance(knight));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, savage1},
				new PhysicalCardImpl[]{scout, savage2});
		// The Knight (6) beats his Savage (5); then, in the skirmish after it, the bonus still holds.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(scout);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetResistance(scout));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetResistance(scout));
		assertEquals(7, scn.GetResistance(knight));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithoutAGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var wish = scn.GetFreepsCard("wish");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(elendil);
		scn.MoveCardsToSupportArea(wish);
		scn.AddTokensToCard(wish, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(wish));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elendil));
		assertEquals(7, scn.GetResistance(elendil));
		assertEquals(2, scn.GetCultureTokensOn(wish));
	}
}
