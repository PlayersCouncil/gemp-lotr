package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chaser", "15_157");
					put("chaser2", "15_157");   // a second copy: each copy answers its own wins alone
					// Hunters of both sides, besides the Chasing Uruk itself:
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2; Hunter 6 is its whole text
					put("faramir", "18_48");    // Faramir, Captain of Ithilien: [gondor] companion, hunter 2; a Maneuver heal used only on purpose
					put("guardian", "17_7");    // Elven Guardian: [elven] companion, hunter 1; +2 against a wounded minion, a Maneuver heal used only on purpose
					// Minions who are no hunters:
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no Ugluk; damage +1 is its whole text
					put("ugluk", "4_176");      // Ugluk, Servant of Saruman: the [isengard] Ugluk, no hunter; tracker and fierce, standing texts on trackers

					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text but knight
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chaser", "15_157");
					put("ugluk", "15_172");     // Uglúk, Ugly Fellow: the Uglúk to spot; fierce, his own text is about his skirmishes
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChasingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Chasing Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>Each time this minion wins a skirmish, you may spot 4 hunters (or Uglúk) to control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chaser");

		assertEquals("Chasing Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var chaser = scn.GetShadowCard("chaser");
		var ugluk = scn.GetShadowCard("ugluk");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(chaser, ugluk);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, chaser);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 10 + 1 for hunter 1 against 6: the Knight loses, and takes a wound.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chaser));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		// Both minions are fierce: the fierce round's assignment actions are next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var chaser = scn.GetShadowCard("chaser");
		var ugluk = scn.GetShadowCard("ugluk");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(chaser, ugluk);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, chaser);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Chasing Uruk has won, beside Uglúk.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chaser));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void EachWinWithFourHuntersOfEitherSideTakesASiteAndAnotherMinionsWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaser = scn.GetShadowCard("chaser");
		var butcher = scn.GetShadowCard("butcher");
		var faramir = scn.GetFreepsCard("faramir");
		var guardian = scn.GetFreepsCard("guardian");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Player 1's third turn: the fellowship moves on to site 4, with sites 1 and 2 behind both markers.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(faramir, guardian, knight, aragorn);
		scn.MoveMinionsToTable(chaser, butcher);
		// Exactly 4 hunters: the Chasing Uruk itself, the Butcher, Faramir and the Guardian.
		assertTrue(scn.HasKeyword(chaser, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(butcher, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(guardian, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(knight, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.HUNTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, chaser}, new PhysicalCardImpl[]{aragorn, butcher});
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));

		// Another minion's win: 5 + 6 for hunter 6 against 8. Nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chaser));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Its own win: 10 + 1 for hunter 1 against 6.
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chaser));
		scn.ShadowAcceptOptionalTrigger();

		// Site 1, the lowest behind every marker, is the Shadow player's.
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Its fierce win: offered again, and site 2 is taken.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, chaser);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chaser));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeHuntersAndAnUrukHaiWhoIsNeitherHunterNorUglukItsWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaser = scn.GetShadowCard("chaser");
		var savage = scn.GetShadowCard("savage");
		var faramir = scn.GetFreepsCard("faramir");
		var guardian = scn.GetFreepsCard("guardian");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(faramir, guardian, knight);
		scn.MoveMinionsToTable(chaser, savage);
		// 3 hunters: the Chasing Uruk, Faramir and the Guardian; the Savage nearly counts, being neither.
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(guardian, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(savage, Keyword.HUNTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, chaser);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chaser));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void LosingASkirmishWithFourHuntersOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaser = scn.GetShadowCard("chaser");
		var butcher = scn.GetShadowCard("butcher");
		var faramir = scn.GetFreepsCard("faramir");
		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(faramir, guardian, aragorn);
		scn.MoveMinionsToTable(chaser, butcher);
		// 8 + 4: enough to beat the Chasing Uruk's 10 + 1 without killing it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chaser);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(11, scn.GetStrength(chaser));
		assertEquals(0, scn.GetWoundsOn(chaser));

		scn.PassCurrentPhaseActions();
		// It loses, takes a wound and lives (vitality 2), with 4 hunters in play and a site to take.
		assertEquals(1, scn.GetWoundsOn(chaser));
		assertInPlay(chaser);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chaser));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithUglukServantOfSarumanDeclinedItTakesNothingAndItsFierceWinOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chaser = scn.GetShadowCard("chaser");
		var chaser2 = scn.GetShadowCard("chaser2");
		var ugluk = scn.GetShadowCard("ugluk");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(chaser, chaser2, ugluk);
		// The [isengard] Ugluk is no hunter: the two Chasing Uruks are the only hunters in play.
		assertFalse(scn.HasKeyword(ugluk, Keyword.HUNTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, chaser);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		// The copy that won makes the offer, and the other copy makes none.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chaser));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chaser2));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// All three are fierce; the Knight takes the Chasing Uruk again and loses again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, chaser);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chaser));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
