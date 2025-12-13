package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_007_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stash", "103_7");
					put("gandalf", "1_364");
					put("radagast", "9_26"); // Another Wizard
					put("gandalfspipe", "1_74");
					put("frodospipe", "3_107");
					put("aragorn", "3_38");
					put("aragornspipe", "1_91");
					put("leaf", "1_300");
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardsStashStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Wizard's Stash
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Pipeweed. Bearer must be a Wizard.
		* 	Each companion who is bearing a pipe is strength +1.
		* 	Regroup: Exert bearer and discard this. This phase you may use special abilities on pipes as if it were the Fellowship phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stash");

		assertEquals("Wizard's Stash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WizardsStashAttachesToWizardAndGivesStrengthToCompanionsWithPipes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var stash = scn.GetFreepsCard("stash");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var frodospipe = scn.GetFreepsCard("frodospipe");

		scn.MoveCompanionsToTable(gandalf, radagast, aragorn);
		scn.AttachCardsTo(gandalf, gandalfspipe);
		scn.AttachCardsTo(frodo, frodospipe);
		// Aragorn has no pipe
		scn.MoveCardsToHand(stash);

		scn.StartGame();

		int gandalfBaseStrength = scn.GetStrength(gandalf);
		int frodoBaseStrength = scn.GetStrength(frodo);
		int radagastBaseStrength = scn.GetStrength(radagast);
		int aragornBaseStrength = scn.GetStrength(aragorn);

		// Wizard's Stash should be playable on either Gandalf or Radagast (both Wizards)
		scn.FreepsPlayCard(stash);

		// Should have 2 valid targets (Gandalf and Radagast)
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoicesAvailable(gandalf, radagast));

		scn.FreepsChooseCard(gandalf); // Choose Gandalf as bearer

		assertAttachedTo(stash, gandalf);

		// Gandalf bears a pipe - should get +1
		assertEquals(gandalfBaseStrength + 1, scn.GetStrength(gandalf));

		// Frodo bears a pipe - should get +1
		assertEquals(frodoBaseStrength + 1, scn.GetStrength(frodo));

		// Radagast does not bear a pipe - should NOT get +1
		assertEquals(radagastBaseStrength, scn.GetStrength(radagast));

		// Aragorn does not bear a pipe - should NOT get +1
		assertEquals(aragornBaseStrength, scn.GetStrength(aragorn));
	}

	
	@Test
	public void WizardsStashPermitsPipeFellowshipActionsToBeUsedInRegroup() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leaf = scn.GetFreepsCard("leaf");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(leaf, toby);

		scn.AttachCardsTo(gandalf, stash);
		scn.AttachCardsTo(aragorn, aragornspipe);

		scn.StartGame();
		
		assertTrue(scn.FreepsActionAvailable(aragornspipe));
		assertTrue(scn.FreepsActionAvailable(frodo));
		assertFalse(scn.FreepsActionAvailable(stash));

		scn.FreepsPass();
		scn.FreepsChooseAny(); //Aragorn and Site required triggers

		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SUPPORT, leaf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsActionAvailable(aragornspipe));
		assertFalse(scn.FreepsActionAvailable(frodo));
		assertTrue(scn.FreepsActionAvailable(stash));

		scn.FreepsUseCardAction(stash);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.HasKeyword(toby, Keyword.PIPEWEED));
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(aragornspipe));
		assertFalse(scn.FreepsActionAvailable(frodo));
	}
}
