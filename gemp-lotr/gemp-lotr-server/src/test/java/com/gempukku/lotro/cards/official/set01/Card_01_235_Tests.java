package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_235_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "1_235");
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, not fierce; strength -2 to a companion bearing a [wraith] condition (none here)
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, not a Nazgûl; fierce only at 2 wounds on the Ring-bearer (none here)
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, printed fierce; his bonuses need 3 burdens
					put("sword", "1_218");      // Nazgûl Sword: a [wraith] possession, borne by a Nazgûl; damage +1 only at 3 burdens
					put("nelya", "1_233");      // Úlairë Nelya, Lieutenant of Morgul: a [wraith] Nazgûl printed fierce; her own Shadow ability is never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Otsëa, Lieutenant of Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Shadow:</b> Exert Úlairë Otsëa to make a [wraith] minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var otsea = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", otsea.getBlueprint().getTitle());
		assertEquals("Lieutenant of Morgul", otsea.getBlueprint().getSubtitle());
		assertTrue(otsea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, otsea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, otsea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, otsea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, otsea.getBlueprint().getRace());
		assertEquals(4, otsea.getBlueprint().getTwilightCost());
		assertEquals(9, otsea.getBlueprint().getStrength());
		assertEquals(3, otsea.getBlueprint().getVitality());
		assertEquals(3, otsea.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsOtseaToMakeTheChosenWraithMinionFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var sword = scn.GetShadowCard("sword");

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, lemenya, hound, khamul);
		scn.AttachCardsTo(lemenya, sword);
		assertAttachedTo(sword, lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(otsea, Keyword.FIERCE));

		scn.ShadowUseCardAction(otsea);

		assertEquals(1, scn.GetWoundsOn(otsea));
		// Every [wraith] minion, Otsëa herself among them; not the [raider] Nazgûl, nor the [wraith] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(otsea, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileOtseaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, hound);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(2, scn.GetWoundsOn(otsea));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(otsea));
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertEquals(2, scn.GetWoundsOn(otsea));
	}

	@Test
	public void ShadowAbilityIsOfferedAgainWhenEveryWraithMinionIsFierceAlready() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, nelya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertFalse(scn.HasKeyword(otsea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));

		// Nelya is fierce already, and is a [wraith] minion all the same: she is offered.
		scn.ShadowUseCardAction(otsea);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, nelya));
		scn.ShadowChooseCard(otsea);

		assertTrue(scn.HasKeyword(otsea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Every [wraith] minion is fierce now and Otsëa can still exert (1 wound of 3): the ability is offered
		// again, and its cost is paid though the keyword changes nothing.
		assertTrue(scn.ShadowActionAvailable(otsea));
		scn.ShadowUseCardAction(otsea);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, nelya));
		scn.ShadowChooseCard(nelya);

		assertEquals(2, scn.GetWoundsOn(otsea));
		assertTrue(scn.HasKeyword(otsea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Vitality 3 with 2 wounds: exhausted.
		assertFalse(scn.ShadowActionAvailable(otsea));
	}

	@Test
	public void FierceLastsThroughTheFierceRoundAndEndsAtTheRegroupPhase()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, hound);
		scn.FreepsPass();

		scn.ShadowUseCardAction(otsea);
		scn.ShadowChooseCard(hound);
		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));

		// Nobody is assigned, so the fierce round follows at once.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(otsea, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
	}
}
