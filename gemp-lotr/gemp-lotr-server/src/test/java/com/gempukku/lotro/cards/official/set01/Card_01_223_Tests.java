package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static org.junit.Assert.*;

public class Card_01_223_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "1_223");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, vitality 2; strength -2 only to a companion or ally bearing a [wraith] condition
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, vitality 3; Easterling bonuses only at 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("marksman", "1_176");   // Goblin Marksman: a Shadow archer minion, no text but Archer
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf archer companion, vitality 3; an archery ability used only on purpose
					put("garrison", "8_36");    // Garrison of Gondor: [gondor] Man archer companion, vitality 3; defender only beside a fortification
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion who is no archer
					put("rumil", "1_57");       // Rúmil, Elven Protector: an Elf archer ally; an archery ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RelentlessChargeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Relentless Charge
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Nazgûl to wound every archer companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charge");

		assertEquals("Relentless Charge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsANazgulToWoundEveryArcherCompanionAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var arwen = scn.GetFreepsCard("arwen");
		var rumil = scn.GetFreepsCard("rumil");
		scn.MoveCompanionsToTable(legolas, garrison, arwen);
		scn.MoveAlliesToTable(rumil);

		var charge = scn.GetShadowCard("charge");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound, marksman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(charge));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(garrison));
		scn.ShadowPlayCard(charge);

		// The cost: a Nazgûl of either culture; not the [wraith] Orc, who could exert as well.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		// One wound on each archer companion, of two cultures and races.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(garrison));
		// Not a companion who is no archer, an archer ally, an archer minion or the Ring-bearer.
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(marksman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(charge);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var charge = scn.GetShadowCard("charge");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(charge);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		// Vitality 2 with 1 wound: exhausted.  The [wraith] Orc beside her could still exert.
		scn.AddWoundsToChar(lemenya, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(charge));
		assertInHand(charge);
		assertEquals(0, scn.GetWoundsOn(legolas));
	}
}
