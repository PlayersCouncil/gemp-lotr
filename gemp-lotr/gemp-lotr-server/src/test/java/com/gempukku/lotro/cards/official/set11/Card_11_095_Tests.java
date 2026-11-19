package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("poleaxe", "11_95");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion, strength 14, vitality 3; toil only
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, vitality 4; his abilities are
					// used only on purpose
					put("ted", "18_75");
					// Crazed Hillman: a [men] Man minion printed fierce; its bonus is for a fierce skirmish it never fights
					put("hillman", "12_60");
					// Prowling Orc: an [orc] Orc lurker, strength 6, vitality 2; its skirmish ability is never used
					put("prowling", "11_136");
					// Goaded to War: a [men] condition that is no minion; its move trigger adds (1) and reaches no reading
					put("goaded", "12_67");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his ability is never used
					// Ranger of Osgiliath: strength 6, vitality 3; its regroup ability is never used
					put("osgiliath", "7_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PoleaxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Poleaxe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [men] minion.<br><b>Skirmish:</b> Remove (2) to make a [men] minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("poleaxe");

		assertEquals("Poleaxe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMenMinionOfEitherRaceAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var poleaxe = scn.GetShadowCard("poleaxe");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var prowling = scn.GetShadowCard("prowling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(poleaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, ted, prowling);
		assertInHand(poleaxe);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(poleaxe));
		scn.ShadowPlayCard(poleaxe);

		// A [men] Man and a [men] Hobbit; not a minion of another culture, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(prowling, aragorn, scn.GetRingBearer()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertAttachedTo(poleaxe, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesTwoToMakeAMenMinionFierceUntilTheRegroupPhaseAndIsNotOfferedWithOneInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var poleaxe = scn.GetShadowCard("poleaxe");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var hillman = scn.GetShadowCard("hillman");
		var prowling = scn.GetShadowCard("prowling");
		var goaded = scn.GetShadowCard("goaded");
		var aragorn = scn.GetFreepsCard("aragorn");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(aragorn, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, ted, hillman, prowling);
		scn.AttachCardsTo(ted, poleaxe);
		scn.MoveCardsToSupportArea(goaded);
		assertAttachedTo(poleaxe, ted);
		scn.SkipToAssignments();
		// The Legion fights Aragorn; the Orc lurker waits for the Ranger, so that skirmish comes last; Ted (who bears
		// the Poleaxe) and the Hillman are assigned to none.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, legion}, new PhysicalCardImpl[]{osgiliath, prowling});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// One in the pool: not offered.
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(poleaxe));

		// Exactly 2: offered.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(poleaxe, ted);
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(hillman, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(poleaxe));

		scn.ShadowUseCardAction(poleaxe);

		assertEquals(0, scn.GetTwilight());
		// A [men] minion in the skirmish being resolved, one in no skirmish (the bearer), and one already fierce;
		// not a minion of another culture, a [men] card that is no minion, the Poleaxe itself, or a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, ted, hillman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(prowling, goaded, poleaxe, aragorn, osgiliath));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(prowling, Keyword.FIERCE));

		// Aragorn, 8, loses to the Legion, 14.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A later skirmish: Ted is still fierce.  Offered again, and taken on the Hillman, who is fierce already
		// (Q-0245): the pool pays and nothing else changes.
		scn.FreepsResolveSkirmish(osgiliath);
		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(poleaxe));
		scn.ShadowUseCardAction(poleaxe);
		scn.ShadowChooseCard(hillman);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(hillman, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));

		// The Ranger, 6, loses to the Orc, 6.  Ted is fierce in the fierce round, and no longer once the regroup
		// phase begins; the Hillman's own fierce stays.
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(ted, Keyword.FIERCE));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(hillman, Keyword.FIERCE));
	}
}
