package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "17_112");
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, hunter 6, no damage bonus
					put("slayer", "17_131");    // White Hand Slayer: [uruk-hai] Uruk-hai, fierce, no damage bonus; its trigger needs an event in its skirmish
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard; companions resistance -1,
					// and a skirmish ability used only on purpose
					put("saruman", "17_116");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1
					put("party", "1_158");      // Uruk-hai Raiding Party: an [isengard] Uruk-hai, damage +1
					// Southron Explorer: "Regroup: Exert this minion and remove (5) to take control of a site"
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladeoftheWhiteHandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Blade of the White Hand
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [uruk-hai] Uruk-hai.<br>While you can spot 2 controlled sites, this minion is <b>damage +1</b>.<br>While you can spot 4 controlled sites, this minion is <b>damage +3</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Blade of the White Hand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void MayBePlayedOnlyOnAnUrukHaiCultureUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var butcher = scn.GetShadowCard("butcher");
		var slayer = scn.GetShadowCard("slayer");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(blade);
		assertInHand(blade);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, guard, party);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertInHand(blade);
		// An [uruk-hai] minion who is a Wizard, a [sauron] Uruk-hai and an [isengard] Uruk-hai.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(blade));

		scn.MoveMinionsToTable(butcher, slayer);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(blade));

		scn.ShadowPlayCard(blade);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher, slayer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, guard, party));
		scn.ShadowChooseCard(slayer);
		assertInZone(Zone.ATTACHED, blade);
		assertSame(slayer, blade.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TakingASecondSiteMakesTheBearerDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var butcher = scn.GetShadowCard("butcher");
		var slayer = scn.GetShadowCard("slayer");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		// Player 1's third turn: the Shadow player's marker is on site 3 as well.
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes stand here.
		scn.MoveMinionsToTable(butcher, slayer, explorer);
		scn.AttachCardsTo(butcher, blade);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();
		assertSame(butcher, blade.getAttachedTo());
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		// One controlled site: no bonus.
		assertEquals(0, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(slayer, Keyword.DAMAGE));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		// Two: the bearer is damage +1, and the other Uruk-hai is not.
		assertEquals(1, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(slayer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeControlledSitesTheBearerIsDamagePlus1AndWithFourDamagePlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var butcher = scn.GetShadowCard("butcher");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(butcher, slayer);
		scn.AttachCardsTo(butcher, blade);
		assertSame(butcher, blade.getAttachedTo());
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertFalse(scn.ShadowControls(scn.GetSite(4)));
		// Three: still only the first sentence.
		assertEquals(1, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(slayer, Keyword.DAMAGE));

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		// Four: both sentences hold, and damage bonuses add up (CR 4.2 "damage bonus"): 1 + 3.
		assertEquals(4, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(slayer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsIsAControlledSiteToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var butcher = scn.GetShadowCard("butcher");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(butcher, slayer);
		scn.AttachCardsTo(butcher, blade);
		assertSame(butcher, blade.getAttachedTo());
		// Site 1 taken by the Free Peoples player on one of his opponent's turns.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(0, scn.GetKeywordCount(butcher, Keyword.DAMAGE));

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		// Two controlled sites, one in each player's support area.
		assertEquals(1, scn.GetKeywordCount(butcher, Keyword.DAMAGE));

		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		// Four controlled sites: 1 + 3.
		assertEquals(4, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(slayer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
