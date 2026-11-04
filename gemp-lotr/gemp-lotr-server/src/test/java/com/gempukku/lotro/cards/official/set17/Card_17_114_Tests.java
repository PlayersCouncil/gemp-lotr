package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("land", "17_114");

					put("feral", "11_183");      // Feral Uruk: [uruk-hai] Uruk-hai, strength 9, vitality 2, no damage bonus; its text only at a battleground, and this path has none
					put("raiders", "1_158");     // Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai; his ability used only on purpose
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, strength 5

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; his ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; his ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LandHadChangedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Land Had Changed
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Uruk-hai strength +2 (or spot a site you control to make that Uruk- hai strength +3 instead).
		 * You may then liberate a site to make that Uruk-hai strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("land");

		assertEquals("Land Had Changed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoSiteControlledItMakesAnUrukHaiOfEitherCultureStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var raiders = scn.GetShadowCard("raiders");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.MoveMinionsToTable(feral, raiders, saruman, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(feral));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(0, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(land));
		scn.ShadowPlayCard(land);

		// An Uruk-hai of either culture, in the skirmish or not; not the [uruk-hai] Wizard, nor the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(feral, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(feral);

		assertEquals(11, scn.GetStrength(feral));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(0, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertInDiscard(land);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyTheOpponentControllingASiteItIsPlus2AndNoLiberationIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertEquals(9, scn.GetStrength(feral));
		scn.ShadowPlayCard(land);

		// The Uruk is the only one, and is chosen unasked; "a site you control" is not the opponent's.
		assertEquals(11, scn.GetStrength(feral));
		assertEquals(0, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithASiteHeControlsItIsPlus3AndLiberatingThatSiteMakesItPlus2MoreAndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var raiders = scn.GetShadowCard("raiders");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveMinionsToTable(feral, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(9, scn.GetStrength(feral));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));
		scn.ShadowPlayCard(land);
		scn.ShadowChooseCard(feral);

		// 9 + 3, the site spotted, with no question about it.
		assertEquals(12, scn.GetStrength(feral));
		assertTrue(scn.ShadowDecisionAvailable("liberate"));
		scn.ShadowChooseYes();

		// The site goes back on the path; 9 + 3 + 2, and damage +1, on that Uruk-hai alone.
		assertFalse(scn.ShadowControls(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(14, scn.GetStrength(feral));
		assertEquals(1, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheLiberationLeavesThePlus3AndTheSiteControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		scn.ShadowPlayCard(land);
		assertTrue(scn.ShadowDecisionAvailable("liberate"));
		scn.ShadowChooseNo();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(12, scn.GetStrength(feral));
		assertEquals(0, scn.GetKeywordCount(feral, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoSitesOfHisOwnHeChoosesWhichIsLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.MoveMinionsToTable(feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		scn.ShadowPlayCard(land);
		scn.ShadowChooseYes();

		// Both of his own sites are offered, and the one he chooses goes back on the path, the lower one here
		// (Ketura's ruling on Q-0370: his choice, never the opponent's site).
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(site1);
		assertFalse(scn.ShadowControls(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(14, scn.GetStrength(feral));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.MoveMinionsToTable(feral);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(land));
		assertInHand(land);
	}

	@Test
	public void TheBonusesAreGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var land = scn.GetShadowCard("land");
		var feral = scn.GetShadowCard("feral");
		var raiders = scn.GetShadowCard("raiders");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(land);
		assertInHand(land);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveMinionsToTable(feral, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, feral}, new PhysicalCardImpl[]{gimli, raiders});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(land);
		scn.ShadowChooseCard(feral);
		scn.ShadowChooseYes();
		assertEquals(14, scn.GetStrength(feral));
		assertEquals(1, scn.GetKeywordCount(feral, Keyword.DAMAGE));

		// 14 against 8, with damage +1: two wounds.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The next skirmish is a phase of its own: both bonuses are gone.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(feral));
		assertEquals(0, scn.GetKeywordCount(feral, Keyword.DAMAGE));
	}
}
