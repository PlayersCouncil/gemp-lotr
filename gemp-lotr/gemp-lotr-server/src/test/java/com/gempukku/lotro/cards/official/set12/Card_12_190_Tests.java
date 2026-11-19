package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_190_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");         // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
					put("gimli", "1_13");           // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose

					put("skulker", "12_95");        // Orc Skulker: [orc] Orc, lurker; strength 5, vitality 2; a discount on skirmish events only
					put("column", "11_72");         // Column of Easterlings: [men] Man, lurker; strength 8; a skirmish ability used only on purpose
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, no lurker; strength 5, vitality 3, damage +1
					put("denizen", "11_115");       // Denizen of Khazad-dum: [orc] Orc, no lurker; stronger underground only
					// Bladed Gauntlets: [orc] possession, "Bearer is a lurker"; a skirmish ability used only on purpose
					put("gauntlets1", "11_109");
					put("gauntlets2", "11_109");
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("pelennor", "12_190");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "11_230");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void NorthernPelennorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Northern Pelennor
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. <b>Plains</b>. <b>Shadow:</b> Make your lurker minion lose <b>lurker</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("pelennor");

		assertEquals("Northern Pelennor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void ShadowAbilityMakesALurkerMinionLoseLurkerThroughTheSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var skulker = scn.GetShadowCard("skulker");
		var column = scn.GetShadowCard("column");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("pelennor"));

		var pelennor = scn.GetCurrentSite();
		assertEquals("Northern Pelennor", pelennor.getBlueprint().getTitle());
		scn.MoveMinionsToTable(skulker, column, savage);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
		assertFalse(scn.HasKeyword(savage, Keyword.LURKER));
		assertTrue(scn.ShadowActionAvailable(pelennor));

		scn.ShadowUseCardAction(pelennor);
		// Lurker minions of two cultures and races; not a minion who is no lurker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(skulker, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(skulker);

		assertFalse(scn.HasKeyword(skulker, Keyword.LURKER));
		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, skulker}, new PhysicalCardImpl[]{boromir, savage},
				new PhysicalCardImpl[]{gimli, column});

		// The Skulker is no lurker now, so its skirmish may be chosen beside the Savage's; the Column's waits.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));

		// Boromir, 7, beats the Savage, 5.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// In the second skirmish the Skulker is still no lurker.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, skulker));
		assertFalse(scn.HasKeyword(skulker, Keyword.LURKER));
		// Aragorn, 8, beats the Skulker, 5, which lives with 1 wound of 2 vitality.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(skulker));

		// The Column, 8, beats Gimli, 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// At the first regroup decision the Skulker is a lurker again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, skulker);
		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
	}

	@Test
	public void NotOfferedWithNoLurkerMinionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var skulker = scn.GetShadowCard("skulker");
		var savage = scn.GetShadowCard("savage");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("pelennor"));

		var pelennor = scn.GetCurrentSite();
		assertEquals("Northern Pelennor", pelennor.getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage, denizen);
		scn.ShadowRefreshDecision();

		// Two minions and no lurker among them: the ability would do nothing.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.LURKER));
		assertFalse(scn.HasKeyword(denizen, Keyword.LURKER));
		assertFalse(scn.ShadowActionAvailable(pelennor));

		scn.MoveMinionsToTable(skulker);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(pelennor));

		// The maneuver phase, with the lurker still there: a Shadow ability only.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
		assertFalse(scn.ShadowActionAvailable(pelennor));
	}

	@Test
	public void ALurkerLostIsNotGainedAgainByAnotherCardBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var skulker = scn.GetShadowCard("skulker");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets1 = scn.GetShadowCard("gauntlets1");
		var gauntlets2 = scn.GetShadowCard("gauntlets2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("pelennor"));

		var pelennor = scn.GetCurrentSite();
		assertEquals("Northern Pelennor", pelennor.getBlueprint().getTitle());
		scn.MoveMinionsToTable(skulker, denizen);
		scn.MoveCardsToHand(gauntlets1, gauntlets2);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
		assertFalse(scn.HasKeyword(denizen, Keyword.LURKER));
		assertInHand(gauntlets1, gauntlets2);

		// The Skulker is the only lurker minion, and is chosen unasked.
		scn.ShadowUseCardAction(pelennor);
		assertFalse(scn.HasKeyword(skulker, Keyword.LURKER));

		// "Bearer is a lurker" on the Skulker gives back nothing: lose beats gain.
		scn.ShadowPlayCard(gauntlets1);
		scn.ShadowChooseCard(skulker);
		assertAttachedTo(gauntlets1, skulker);
		assertFalse(scn.HasKeyword(skulker, Keyword.LURKER));

		// The same card on the Denizen, who lost nothing, makes him a lurker.
		scn.ShadowPlayCard(gauntlets2);
		assertAttachedTo(gauntlets2, denizen);
		assertTrue(scn.HasKeyword(denizen, Keyword.LURKER));
		assertFalse(scn.HasKeyword(skulker, Keyword.LURKER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
