package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "15_103");
					put("runner2", "15_103");     // a second copy: not unique
					put("scurrying", "11_137");   // Scurrying Goblin: [orc] Orc, cost 1, strength 5; its (2) only when played at an underground site
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc of another title; +2 only at an underground site
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; a start-of-assignment trigger only
					put("goblin", "1_178");       // Goblin Runner: a [moria] Orc, cost 1, strength 5; adds (2) when played
					put("corps", "11_73");        // Corps of Harad: [men] Man minion, strength 10; fierce only at a battleground (none on this path)
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] Orc.<br>While you can spot more sites on the adventure path than minions, this minion is strength +5.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("runner");

		assertEquals("Black Land Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToPlayItNeedsAnOrcOrcAndAnOrcHalfTrollOrAMoriaOrcWillNotDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scurrying = scn.GetShadowCard("scurrying");
		var halftroll = scn.GetShadowCard("halftroll");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveCardsToHand(runner, scurrying);
		assertInZone(Zone.HAND, runner);
		assertInZone(Zone.HAND, scurrying);
		scn.MoveMinionsToTable(halftroll, goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// An [orc] Half-troll and a [moria] Orc: no [orc] Orc to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(runner));

		scn.ShadowPlayCard(scurrying);
		assertInZone(Zone.SHADOW_CHARACTERS, scurrying);
		assertTrue(scn.ShadowPlayAvailable(runner));
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ToPlayAnOrcOrcOfAnotherTitleWillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveCardsToHand(runner);
		assertInZone(Zone.HAND, runner);
		scn.MoveMinionsToTable(denizen);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(runner));
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileMoreSitesThanMinionsItIsStrengthPlus5AndAFifthMinionPlayedEndsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var halftroll = scn.GetShadowCard("halftroll");
		var corps = scn.GetShadowCard("corps");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		// Both markers on site 4; the move takes the fellowship to site 5: five sites on the adventure path.
		scn.SkipToSite(4);
		scn.MoveCardsToHand(goblin);
		assertInZone(Zone.HAND, goblin);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(runner, runner2, halftroll, corps);
		scn.ShadowRefreshDecision();

		// Four minions of two cultures and three races against five sites: each Runner 9 + 5 by its own text, the
		// other copy adding nothing to it. Counted as characters, the two companions would make six against five,
		// and no bonus.
		assertEquals(14, scn.GetStrength(runner));
		assertEquals(14, scn.GetStrength(runner2));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(10, scn.GetStrength(corps));

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);

		// Five minions against five sites: no more sites than minions.
		assertEquals(9, scn.GetStrength(runner));
		assertEquals(9, scn.GetStrength(runner2));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(5, scn.GetStrength(goblin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteTheShadowPlayerControlsIsNoLongerOnTheAdventurePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scurrying = scn.GetShadowCard("scurrying");
		var halftroll = scn.GetShadowCard("halftroll");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		var site1 = scn.GetSite(1);

		scn.MoveMinionsToTable(runner, scurrying, halftroll, corps);
		scn.ShadowRefreshDecision();

		// Five sites against four minions: 9 + 5.
		assertEquals(14, scn.GetStrength(runner));
		assertFalse(scn.ShadowControls(site1));

		// Site 1, behind both markers, taken under control: it goes to the Shadow player's support area, and four
		// sites are left on the path against four minions.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		assertEquals(9, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
