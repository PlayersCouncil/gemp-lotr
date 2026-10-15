package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_00_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "0_60");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTokenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "0_60");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a third companion, and the bearer of Desperate Defense
					put("smeagol", "7_71");     // Sméagol, Always Helps: a Free Peoples card that adds a burden (to be played) and 2 threats (his assignment ability)

					put("towers", "7_173");     // War Towers: [raider] Maneuver event, cost 1, "Spot a [raider] minion to add a threat" (one more for each companion over 4)
					put("nomad", "7_132");      // Desert Nomad: [raider] Man, vitality 2; damage +1 at 2 threats and nothing else
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition on a companion; each time he is assigned, discard 3 cards from hand or add a burden
					put("runner", "1_178");     // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "0_60");
					put("smeagol", "103_116");   // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; to play, add a burden; an assignment ability only
					put("bounder", "1_286");     // Bounder: a [shire] Hobbit ALLY; a skirmish ability only
					put("pippin", "103_31");     // Pippin, Trollslayer: a Hobbit companion who is [gondor]; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "0_60");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North, and
					put("gimli", "1_13");        // Gimli, Son of Gloin: with the Ring-bearer, three companions hold 3 threats
					put("keptsafe", "6_111");    // Kept Safe: [shire] condition that gets 2 [shire] tokens as it is played; a response to a Hobbit's wound only

					put("brute", "7_188");       // Morgul Brute: [wraith] Orc; "When you play this minion, you may spot a Nazgul to add a burden"
					put("destroyer", "7_190");   // Morgul Destroyer: [wraith] Orc; "When you play this minion, you may spot a Nazgul to add 2 threats"
					put("lemenya", "1_232");     // Ulaire Lemenya: the Nazgul spotted; his text touches only bearers of [wraith] conditions
					put("towers", "7_173");      // War Towers: [raider] Maneuver event, cost 1, "Spot a [raider] minion to add a threat"
					put("nomad", "7_132");       // Desert Nomad: [raider] Man; damage +1 at 2 threats and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path with City Gates as its site 5: "When the fellowship moves to this site, add 3 threats." A site is a
	// card of neither side.
	protected VirtualTableScenario GetCityGatesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "0_60");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North, and
					put("knight", "5_35");       // Gondorian Knight ("Knight." is its whole text): three companions hold 3 threats
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.KingSites);
					put("site5", "7_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TomBombadilsHatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Tom Bombadil's Hat
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [shire] companions.<br>Each time a Shadow card adds a burden or a threat, add a [shire] token here.<br><b>Fellowship:</b> Remove 2 [shire] tokens from here to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hat");

		assertEquals("Tom Bombadil's Hat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AThreatAddedByAShadowCardAddsAShireTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var towers = scn.GetShadowCard("towers");
		var nomad = scn.GetShadowCard("nomad");
		scn.MoveCardsToSupportArea(hat);
		// Three companions with the Ring-bearer: room for 3 threats, and none over 4 for War Towers to count.
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(towers);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(hat));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(towers));
		scn.ShadowPlayCard(towers);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Two threats added by a Free Peoples card, as the cost of Sméagol's ability, add no token.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void ABurdenAddedByAShadowCardAddsAShireTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hat);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveMinionsToTable(runner);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetCultureTokensOn(hat));

		// The burden Sméagol adds to be played is added by a Free Peoples card: no token.
		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetCultureTokensOn(hat));
		assertEquals(0, scn.GetFreepsHandCount());

		// With no card in hand the Free Peoples player cannot discard 3, so Desperate Defense adds its burden
		// as Aragorn is assigned: a burden added by a Shadow card.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void CannotBePlayedUntilTwoShireCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var hat = scn.GetFreepsCard("hat");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(hat, smeagol);
		assertInZone(Zone.HAND, hat, smeagol);

		scn.StartGame();

		// Frodo is one [shire] companion; Bounder is a [shire] ally, and Pippin a Hobbit companion of [gondor].
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hat));

		// Smeagol is a second [shire] companion, and no Hobbit.
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(hat));

		scn.FreepsPlayCard(hat);
		assertInZone(Zone.SUPPORT, hat);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAShadowCardAddsABurdenOrAThreatAnotherTokenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var brute = scn.GetShadowCard("brute");
		var lemenya = scn.GetShadowCard("lemenya");
		var towers = scn.GetShadowCard("towers");
		var nomad = scn.GetShadowCard("nomad");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		scn.MoveCardsToSupportArea(hat);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(brute, towers, keptsafe);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nomad);
		scn.FreepsRefreshDecision();
		// Kept Safe holds 2 [shire] tokens of its own: a card that "here" must not reach.
		scn.FreepsPlayCard(keptsafe);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(hat));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));

		// A minion adds a burden.
		scn.ShadowPlayCard(brute);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));

		// An event adds a threat: a second token.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(towers);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetCultureTokensOn(hat));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TwoThreatsAddedAtOnceByAShadowCardAddTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(hat);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(hat));

		scn.ShadowPlayCard(destroyer);
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseNo();
		assertEquals(2, scn.GetThreats());
		// Each threat is a threat added.
		assertEquals(2, scn.GetCultureTokensOn(hat));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThreatsASiteAddsAddNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCityGatesScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToSupportArea(hat);
		scn.MoveCompanionsToTable(aragorn, knight);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(hat));

		scn.SkipToSite(5);
		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(hat));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionRemovesTwoShireTokensToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var hat = scn.GetFreepsCard("hat");
		scn.MoveCardsToSupportArea(hat);
		scn.AddTokensToCard(hat, 3);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(3, scn.GetCultureTokensOn(hat));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(hat));

		scn.FreepsUseCardAction(hat);
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithOneTokenHereThoughAnotherShireCardHoldsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var hat = scn.GetFreepsCard("hat");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		scn.MoveCardsToSupportArea(hat);
		scn.AddTokensToCard(hat, 1);
		scn.MoveCardsToHand(keptsafe);
		assertInZone(Zone.HAND, keptsafe);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPlayCard(keptsafe);

		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hat));
	}
}
