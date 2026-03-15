package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illwind", "103_67");
					put("nazgul1", "1_232");   // Ulaire Enquea
					put("nazgul2", "1_234");   // Ulaire Nertea
					put("witchking", "1_237"); // The Witch-king, Lord of Angmar
					put("guard", "1_7");       // Dwarf Guard (VIT 2)
					put("gimli", "1_12");      // Gimli, Son of Gloin (VIT 3)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IllWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ill Wind
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: The second time you play a Nazgul each Shadow phase, you may hinder a wounded companion.
		 *   The Free Peoples player may make you exert another companion to prevent this
		 *   (or wound if you can spot The Witch-king).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("illwind");

		assertEquals("Ill Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IllWindDoesNotTriggerOnFirstNazgulPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, nazgul2);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul -- should NOT trigger Ill Wind
		scn.ShadowPlayCard(nazgul1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void IllWindTriggersOnSecondNazgulPlayedAndHindersWoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, nazgul2);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul
		scn.ShadowPlayCard(nazgul1);

		// Play second Nazgul -- SHOULD trigger Ill Wind
		scn.ShadowPlayCard(nazgul2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// FP declines prevention
		scn.FreepsChooseNo();
		assertTrue(scn.IsHindered(guard));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IllWindDoesNotTriggerOnThirdNazgulPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var nazgul3 = scn.GetShadowCard("witchking");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, nazgul2, nazgul3);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul -- should NOT trigger Ill Wind
		scn.ShadowPlayCard(nazgul1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Play second Nazgul -- SHOULD trigger Ill Wind
		scn.ShadowPlayCard(nazgul2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Play third Nazgul -- should NOT trigger Ill Wind
		scn.ShadowPlayCard(nazgul3);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void IllWindPreventionExertsAnotherCompanionWithoutWitchKing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, nazgul2);

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nazgul1);
		scn.ShadowPlayCard(nazgul2);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gimli);

		// FP accepts prevention
		scn.FreepsChooseYes();
		//Guard is already exhausted and so can't be targeted, Gimli can't be targeted as the hinder target, so
		// Frodo is auto-targeted as the only remaining choice.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(gimli));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IllWindPreventionWoundsAnotherCompanionWithWitchKing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, witchking);

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nazgul1);
		scn.ShadowPlayCard(witchking);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gimli);

		// FP accepts prevention
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoicesAvailable(frodo, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));

		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.IsHindered(gimli));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

}
