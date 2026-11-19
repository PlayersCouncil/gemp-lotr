package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_272_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("engineer", "7_272");
					put("engineer2", "7_272");
					put("battlement", "7_262");  // Above the Battlement: plays a besieger stacked on a site you control (or removes a burden to play a [sauron] Orc from the discard pile)
					put("teeth", "5_110");       // Teeth of Mordor: Shadow: play a [sauron] Orc stacked on this condition as if played from hand
					put("pillager", "7_275");    // Gorgoroth Pillager: another besieger Orc, cost 4, site 5; his text works only while he is stacked
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, so that the threat limit is 3; a maneuver ability used only on purpose
					put("dwarf", "1_7");        // Dwarf Guard: a companion with no text once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothEngineerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Engineer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.
		 * When you play this minion from a site you control, add 2 threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("engineer");

		assertEquals("Gorgoroth Engineer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromASiteYouControlItAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var engineer = scn.GetShadowCard("engineer");
		var battlement = scn.GetShadowCard("battlement");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("dwarf"));

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, engineer);
		assertInZone(Zone.STACKED, engineer);
		scn.SetShadowHand(battlement);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		// Three companions: room for 3 threats.
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No burden to remove, so the Engineer on the site is played with no question.
		scn.ShadowPlayCard(battlement);

		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromHandOrFromAConditionItAddsNoThreatNorDoesAnotherBesiegerPlayedFromTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var engineer = scn.GetShadowCard("engineer");
		var engineer2 = scn.GetShadowCard("engineer2");
		var battlement = scn.GetShadowCard("battlement");
		var teeth = scn.GetShadowCard("teeth");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("dwarf"));

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// One Engineer in hand, one stacked on Teeth of Mordor (a condition, not a site); a Pillager on the site.
		scn.StackCardsOn(site1, pillager);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, engineer2);
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		assertTrue(scn.GetStackedCards(teeth).contains(engineer2));
		assertInZone(Zone.STACKED, pillager, engineer2);
		scn.SetShadowHand(engineer, battlement);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(engineer);
		assertInZone(Zone.SHADOW_CHARACTERS, engineer);
		assertEquals(0, scn.GetThreats());

		scn.ShadowUseCardAction(teeth);
		assertInZone(Zone.SHADOW_CHARACTERS, engineer2);
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(battlement);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
