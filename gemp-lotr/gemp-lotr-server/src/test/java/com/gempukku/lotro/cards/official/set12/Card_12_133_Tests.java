package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tolman", "12_133");
					// Desperate Defense of the Ring: a [sauron] condition borne by a companion. "Each time bearer is
					// assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from hand or
					// add a burden." (to play, exert a [sauron] Orc: cheated on)
					put("defense", "1_244");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; "When you play" only
					// Orc Miscreant: an [orc] Orc minion, strength 7, vitality 3, "Regroup: Exert this minion to add a
					// burden." (to play, spot an [orc] minion: cheated in)
					put("miscreant", "11_131");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: to bear Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand. Remove (1) for each
					// Orc revealed."
					put("glamdring", "1_75");
					// Eye of Barad-Dur: a [sauron] event; "Response: If a Free Peoples player reveals this card from
					// your hand, discard this card to add 2 burdens."
					put("eye", "5_96");
					// Goblin Spear: [moria] hand weapon, strength +2, on a [moria] minion; "If bearer kills a companion
					// in a skirmish, add 1 burden (or 2 burdens if that companion was a Hobbit)."
					put("spear", "1_182");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// not used
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer: a Dwarf, so that Tolman can be the only Hobbit. Gimli's own
	// burdens come at the start of a skirmish involving him, and he is never assigned here.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tolman", "12_133");
					put("defense", "1_244");    // Desperate Defense of the Ring, as above
					put("runner", "1_178");     // Goblin Runner, as above
					// Aragorn, Ranger of the North: [gondor] Man companion, the bearer of the Defense
					put("aragorn", "1_89");
					// Bounder: [shire] Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3, vitality 4; his maneuver
					// ability needs a site his player controls and his regroup ability is used only on purpose
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Eregion Hills as site 4: "When the fellowship moves to Eregion Hills, add a burden."
	protected VirtualTableScenario GetEregionHillsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tolman", "12_133");
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

	@Test
	public void TolmanCottonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Tolman Cotton, Farmer of Bywater
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 4
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Response:</b> If a burden is about to be added by a Shadow card, spot another Hobbit and exert Tolman Cotton to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tolman");

		assertEquals("Tolman Cotton", card.getBlueprint().getTitle());
		assertEquals("Farmer of Bywater", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void ResponseExertsTolmanToPreventABurdenAShadowConditionIsAboutToAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(tolman);

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(defense, frodo);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));

		// With no card in hand the burden is all the Free Peoples player can choose, and nothing is asked. Frodo is
		// the other Hobbit spotted.
		scn.FreepsAssignToMinions(frodo, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tolman));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithNoOtherHobbitInPlayTolmanIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(tolman, aragorn);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(defense, aragorn);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));

		// Gimli the Ring-bearer is a Dwarf and Aragorn a Man: Tolman is the only Hobbit.
		scn.FreepsAssignToMinions(aragorn, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tolman));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AHobbitAllyIsAnotherHobbitHeCanSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(tolman, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(defense, aragorn);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));

		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tolman));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayersHobbitMinionIsAnotherHobbitHeCanSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(tolman, aragorn);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveMinionsToTable(runner, ted);
		assertAttachedTo(defense, aragorn);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{tolman, ted});
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tolman));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ExertsToPreventTheBurdenAShadowMinionsAbilityAddsAndExhaustedCannotPreventTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var frodo = scn.GetRingBearer();
		var miscreant = scn.GetShadowCard("miscreant");
		scn.MoveCompanionsToTable(tolman);
		scn.AddWoundsToChar(tolman, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(tolman));
		scn.MoveMinionsToTable(miscreant);
		scn.SkipToAssignments();
		// Frodo takes the Miscreant, which comes out of the skirmish unwounded.
		scn.FreepsAssignToMinions(frodo, miscreant);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		// The Ruling Ring's offer for Frodo's wound is declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(miscreant));
		assertTrue(scn.ShadowActionAvailable(miscreant));

		scn.ShadowUseCardAction(miscreant);
		assertEquals(1, scn.GetWoundsOn(miscreant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(tolman));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Vitality 4 with 3 wounds: he cannot exert again, and the next burden is added.
		scn.FreepsPass();
		scn.ShadowUseCardAction(miscreant);
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tolman));
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(tolman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfTwoBurdensAShadowEventAddsAtOnceEachUsePreventsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		scn.MoveCompanionsToTable(tolman, gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.HAND, eye);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));

		// Glamdring reveals the Shadow hand, and the Eye answers it.
		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		scn.ShadowPlayCard(eye);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tolman));
		// That one burden is prevented; he is offered again for the second, and declines.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tolman));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PreventsTheBurdenAShadowPossessionAddsWhenItsBearerKillsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(tolman, aragorn);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		assertAttachedTo(spear, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));

		// 15 against 8: Aragorn loses, and his fourth wound kills him.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tolman));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(tolman));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenAFreePeoplesCardOrASiteAddsIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEregionHillsScenario();

		var tolman = scn.GetFreepsCard("tolman");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(tolman);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(smeagol);
		scn.FreepsRefreshDecision();

		var burdens = scn.GetBurdens();
		assertInZone(Zone.HAND, smeagol);
		assertEquals(0, scn.GetWoundsOn(tolman));

		// Smeagol, a Free Peoples card, adds a burden to be played: Tolman is not offered.
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tolman));
		assertEquals(burdens + 1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Eregion Hills, a site, adds one as the fellowship moves to it: nor is he offered then.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tolman));
		assertEquals(burdens + 2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(tolman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
