package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("melilot", "3_110");
					// Orc Miscreant: an [orc] Orc minion, vitality 3, "Regroup: Exert this minion to add a burden." (to
					// play, spot an [orc] minion: cheated in)
					put("miscreant", "11_131");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: to bear Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand. Remove (1) for each
					// Orc revealed."
					put("glamdring", "1_75");
					// Eye of Barad-Dur: a [sauron] event; "Response: If a Free Peoples player reveals this card from your
					// hand, discard this card to add 2 burdens."
					put("eye", "5_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Eregion Hills as site 4: "When the fellowship moves to Eregion Hills, add a burden."
	protected VirtualTableScenario GetEregionHillsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("melilot", "3_110");
					// Smeagol, Always Helps: a [gollum] companion, "To play, add a burden."; an assignment ability only
					put("smeagol", "7_71");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "3_116");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("melilot", "3_110");
					// Desperate Defense of the Ring: a [sauron] condition borne by a companion.  "Each time bearer
					// is assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from
					// hand or add a burden."
					put("defense", "1_244");
					put("runner", "1_178");     // Goblin Runner: the minion its bearer is assigned to
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MelilotBrandybuckStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Melilot Brandybuck, Merry Dancer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: <b>Response:</b> If a burden is about to be added by a Shadow card, exert Melilot Brandybuck to prevent that burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("melilot");

		assertEquals("Melilot Brandybuck", card.getBlueprint().getTitle());
		assertEquals("Merry Dancer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void ResponseExertsMelilotToPreventABurdenAShadowCardIsAboutToAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var melilot = scn.GetFreepsCard("melilot");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveAlliesToTable(melilot);

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(defense, frodo);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(melilot));

		// With no card in hand the burden is all the Free Peoples player can choose, and nothing is asked.
		scn.FreepsAssignToMinions(frodo, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(defense, frodo);
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ExertsToPreventTheBurdenAShadowMinionsAbilityAddsAndExhaustedCannotPreventTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var melilot = scn.GetFreepsCard("melilot");
		var miscreant = scn.GetShadowCard("miscreant");
		scn.MoveAlliesToTable(melilot);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(melilot));
		assertEquals(0, scn.GetWoundsOn(miscreant));
		assertTrue(scn.ShadowActionAvailable(miscreant));

		scn.ShadowUseCardAction(miscreant);
		assertEquals(1, scn.GetWoundsOn(miscreant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseOptionalTrigger(melilot);
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Vitality 2 with 1 wound: she cannot exert again, and the next burden is added.
		scn.FreepsPass();
		scn.ShadowUseCardAction(miscreant);
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfTwoBurdensAShadowEventAddsAtOnceSheExertsToPreventOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var melilot = scn.GetFreepsCard("melilot");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		scn.MoveAlliesToTable(melilot);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.HAND, eye);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(melilot));

		// Glamdring reveals the Shadow hand, and the Eye answers it.
		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		scn.ShadowPlayCard(eye);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));

		scn.FreepsChooseOptionalTrigger(melilot);
		// She prevents that one burden; exhausted, she can prevent no other, and the second is added.
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenAFreePeoplesCardOrASiteAddsIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEregionHillsScenario();

		var melilot = scn.GetFreepsCard("melilot");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveAlliesToTable(melilot);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(smeagol);
		scn.FreepsRefreshDecision();

		var burdens = scn.GetBurdens();
		assertInZone(Zone.HAND, smeagol);
		assertEquals(0, scn.GetWoundsOn(melilot));

		// Smeagol, a Free Peoples card, adds a burden to be played: Melilot is not offered.
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertEquals(burdens + 1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Eregion Hills, a site, adds one as the fellowship moves to it: nor is she offered then.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertEquals(burdens + 2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(melilot));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
