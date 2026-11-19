package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarm", "10_87");
					put("swarm2", "10_87");

					put("pillager", "7_275");      // Gorgoroth Pillager: besieger Orc, cost 4; its text works only while stacked on a site
					put("garrison", "7_273");      // Gorgoroth Garrison: besieger Orc, cost 3; two Regroup abilities only
					put("troll", "8_102");         // Great Hill Troll: besieger Troll, cost 9; its play trigger is for sites 5K to 9K only
					put("urukbesieger", "4_180");  // Uruk Besieger: [isengard] Uruk-hai, cost 2, "Besieger" in its title and NOT its keyword
					put("mordorpillager", "10_92"); // Mordor Pillager: besieger Orc, cost 3; a Shadow ability with a cost

					put("gimli", "1_13");          // Gimli, Son of Gloin
					put("legolas", "1_51");        // Legolas, Prince of Mirkwood
					put("aragorn", "1_89");        // Aragorn, Ranger of the North
					put("boromir", "1_97");        // Boromir, Son of Denethor
					put("merry", "1_303");         // Merry, From O'er the Brandywine
					put("barliman", "1_70");       // Barliman Butterbur: a [gandalf] ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothSwarmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gorgoroth Swarm
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br>When you play this minion, you may spot 6 companions and another besieger to play a besieger from your discard pile and control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swarm");

		assertEquals("Gorgoroth Swarm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithSixCompanionsAndAnotherBesiegerItPlaysABesiegerFromTheDiscardPileAndControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo and five more: six companions.
		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");
		// The Free Peoples player's own besieger, of a title the Shadow player's pile does not hold.
		var fpmordorpillager = scn.GetFreepsCard("mordorpillager");
		scn.MoveCardsToFreepsDiscard("mordorpillager");

		var swarm = scn.GetShadowCard("swarm");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");
		var troll = scn.GetShadowCard("troll");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("garrison", "troll", "urukbesieger");

		scn.StartGame();
		// Player 1's second turn, both markers on site 2: site 1 can be taken.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager);
		assertInDiscard(garrison, troll, urukbesieger, fpmordorpillager);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		var site1 = scn.GetSite(1);
		assertFalse(scn.ShadowControls(site1));

		scn.ShadowPlayCard(swarm);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarm));
		scn.ShadowAcceptOptionalTrigger();

		// Two besiegers of different races are offered; the [isengard] "Besieger" by title and the other player's own are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(garrison, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(urukbesieger, fpmordorpillager));
		scn.ShadowChooseCards(garrison);

		assertInZone(Zone.SHADOW_CHARACTERS, swarm, garrison);
		assertInDiscard(troll, urukbesieger, fpmordorpillager);
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABesiegerWhoIsNoOrcIsSpottedAndDecliningLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");

		var swarm = scn.GetShadowCard("swarm");
		var troll = scn.GetShadowCard("troll");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("garrison");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troll);
		assertInDiscard(garrison);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		var site1 = scn.GetSite(1);

		scn.ShadowPlayCard(swarm);
		// The Troll is the other besieger spotted.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarm));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(garrison);
		assertFalse(scn.ShadowControls(site1));
		assertFalse(scn.FreepsControls(site1));
		assertInZone(Zone.SHADOW_CHARACTERS, swarm);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo and four more: five companions; the ally beside them is no companion.
		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir");
		scn.MoveAlliesToTable("barliman");

		var swarm = scn.GetShadowCard("swarm");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("garrison");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager);
		assertInDiscard(garrison);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(swarm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarm));
		assertInDiscard(garrison);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyOtherBesiegerIsOneByTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");

		var swarm = scn.GetShadowCard("swarm");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("garrison");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(urukbesieger);
		assertInDiscard(garrison);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();

		// The Swarm itself is a besieger, but not "another"; the Uruk Besieger is none.
		scn.ShadowPlayCard(swarm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarm));
		assertInDiscard(garrison);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheBesiegerInTheDiscardPileCannotBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");

		var swarm = scn.GetShadowCard("swarm");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("garrison");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager);
		assertInDiscard(garrison);
		scn.FreepsPass();

		// At site 3: the Swarm costs 5 + 2 roaming, the Garrison 3 + 2 roaming; 11 leaves 4 after the Swarm, one short.
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(swarm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarm));
		assertInDiscard(garrison);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoBesiegerInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");

		var swarm = scn.GetShadowCard("swarm");
		var pillager = scn.GetShadowCard("pillager");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("urukbesieger");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pillager);
		assertInDiscard(urukbesieger);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();

		// The play from the discard pile is a cost (Q-0062): with no besieger there, nothing is offered,
		// though a site could be taken.
		scn.ShadowPlayCard(swarm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarm));
		assertInDiscard(urukbesieger);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondSwarmPlayedLaterSetsOffItsOwnTriggerAndNotTheFirsts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");

		var swarm = scn.GetShadowCard("swarm");
		var swarm2 = scn.GetShadowCard("swarm2");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCardsToHand(swarm2);
		scn.MoveCardsToShadowDiscard("pillager");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(swarm);
		assertInDiscard(pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, swarm);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();

		// The first Swarm is the other besieger the second spots; only the second's own play sets off a trigger.
		scn.ShadowPlayCard(swarm2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarm));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarm2));
		scn.ShadowChooseOptionalTrigger(swarm2);

		assertInZone(Zone.SHADOW_CHARACTERS, swarm, swarm2, pillager);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeTakenTheBesiegerIsStillPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "aragorn", "boromir", "merry");

		var swarm = scn.GetShadowCard("swarm");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");
		scn.MoveCardsToHand(swarm);
		scn.MoveCardsToShadowDiscard("garrison");

		// Turn 1: the Shadow player's marker is still on site 1, so no site can be taken.
		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		assertInDiscard(garrison);
		scn.FreepsPass();

		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		var site1 = scn.GetSite(1);

		scn.ShadowPlayCard(swarm);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarm));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, swarm, garrison);
		assertFalse(scn.ShadowControls(site1));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
