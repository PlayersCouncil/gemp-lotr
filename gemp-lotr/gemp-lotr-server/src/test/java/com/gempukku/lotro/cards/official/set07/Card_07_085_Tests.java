package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "7_85");

					put("guard", "13_67");      // Guard of the White Tree: [gondor] knight Man, twilight 3; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] knight who is a Hobbit, twilight 2; to play, spot a [gondor] companion
					put("knight", "5_35");      // Gondorian Knight: [gondor] knight Man, twilight 2; "Knight." is its whole text
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man who is no knight, twilight 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "7_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a third companion; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a fourth companion; "Knight." is its whole text
					put("smeagol", "7_71");     // Sméagol, Always Helps: a Free Peoples card that adds a burden (to be played) and 2 threats (his assignment ability)

					put("towers", "7_173");     // War Towers: [raider] Maneuver event, "Spot a [raider] minion to add a threat" (one more for each companion over 4)
					put("nomad", "7_132");      // Desert Nomad: [raider] Man minion; damage +1 at 2 threats and nothing else
					put("brute", "7_188");      // Morgul Brute: [wraith] Orc; when played, may spot a Nazgûl to add a burden (the Free Peoples player may wound the Ring-bearer to prevent it)
					put("destroyer", "7_190");  // Morgul Destroyer: [wraith] Orc; when played, may spot a Nazgûl to add 2 threats (the same prevention)
					put("lemenya", "1_232");    // Úlairë Lemenya: the Nazgûl spotted; his text touches only bearers of [wraith] conditions
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition on a companion; each time he is assigned, discard 3 cards from hand or add a burden
					put("ax", "18_84");         // Orkish Ax: [orc] hand weapon, strength +2; each time bearer wins a skirmish, its owner may add a threat
					put("champion", "11_111");  // Champion Orc: [orc] Orc, strength 13, vitality 2; toil 2 and nothing else
					put("spear", "1_182");      // Goblin Spear: [moria] hand weapon, strength +2; if bearer kills a companion in a skirmish, add 1 burden (2 for a Hobbit)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Eregion Hills as site 4 ("When the fellowship moves to Eregion Hills, add a burden") and City Gates as site 5
	// ("When the fellowship moves to this site, add 3 threats"): a site is a card of neither side.
	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "7_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a third companion, so that 3 threats can be added
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "3_116");
					put("site5", "7_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenethorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Denethor, Steward of the City
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Fellowship:</b> Play a [gondor] knight. That knight's twilight cost is -2.<br>Each time a Shadow card adds a threat, add a burden. Each time a Shadow card adds a burden, add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denethor");

		assertEquals("Denethor", card.getBlueprint().getTitle());
		assertEquals("Steward of the City", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipActionPlaysAGondorKnightFromHandAtTwoLessForThatKnightAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(denethor);
		scn.MoveCardsToHand(guard, pippin, knight, boromir);

		scn.StartGame();
		assertInZone(Zone.HAND, guard, pippin, knight, boromir);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(denethor));

		scn.FreepsUseCardAction(denethor);
		// Knights who are Men and a knight who is a Hobbit; not a [gondor] Man who is no knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, pippin, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		// 3 - 2
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The next knight played from hand in the ordinary way pays his whole cost: 1 + 2.
		scn.FreepsPlayCard(knight);
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertEquals(3, scn.GetTwilight());

		// The ability has no limit: Pippin, the one knight left in hand, is played unasked, for 2 - 2.
		assertTrue(scn.FreepsActionAvailable(denethor));
		scn.FreepsUseCardAction(denethor);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(3, scn.GetTwilight());
		assertInHand(boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoGondorKnightInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(denethor);
		scn.MoveCardsToHand(boromir);

		scn.StartGame();
		assertInHand(boromir);

		// Boromir can be played, but is no knight.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(boromir));
		assertFalse(scn.FreepsActionAvailable(denethor));
	}

	@Test
	public void AThreatAShadowEventAddsAddsABurdenAndThreatsOrBurdensFreePeoplesCardsAddDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(denethor, aragorn);
		scn.MoveCardsToHand(smeagol);

		var towers = scn.GetShadowCard("towers");
		var nomad = scn.GetShadowCard("nomad");
		scn.MoveCardsToHand(towers);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// Sméagol's "To play, add a burden": a burden a Free Peoples card adds.  No threat.
		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		scn.MoveMinionsToTable(nomad);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// A threat a Shadow event adds: Denethor adds a burden.  That burden is his own, a Free Peoples card's, so no
		// threat follows it; nor does the Shadow card's threat add one.
		scn.ShadowPlayCard(towers);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Two threats added by a Free Peoples card, as the cost of Sméagol's ability: no burden.  Four companions
		// hold the 3 threats.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(3, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TwoThreatsAShadowMinionAddsAtOnceAddTwoBurdensAndALaterThreatOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(denethor, aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		var towers = scn.GetShadowCard("towers");
		var nomad = scn.GetShadowCard("nomad");
		scn.MoveCardsToHand(destroyer, towers);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nomad);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(destroyer);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		// Each threat is a threat added (Q-0322, Q-0324).
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());

		// Another Shadow card's threat, later: a third burden.  Three companions hold the 3 threats.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(towers);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AThreatAShadowPossessionAddsAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(denethor, aragorn);

		var ax = scn.GetShadowCard("ax");
		var champion = scn.GetShadowCard("champion");

		scn.StartGame();
		scn.MoveMinionsToTable(champion);
		scn.AttachCardsTo(champion, ax);
		assertAttachedTo(ax, champion);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, champion);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 15 against 8: the Champion wins, and its Ax adds a threat.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ax));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenAShadowMinionAddsAddsAThreatAndSoDoesALaterOneAShadowConditionAdds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(denethor, aragorn);

		var brute = scn.GetShadowCard("brute");
		var lemenya = scn.GetShadowCard("lemenya");
		var defense = scn.GetShadowCard("defense");
		scn.MoveCardsToHand(brute);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		assertAttachedTo(defense, aragorn);
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// A burden a Shadow minion adds: Denethor adds a threat, and that threat, his own, adds no burden; nor does the
		// Shadow card's burden add one.
		scn.ShadowPlayCard(brute);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());

		// With no card in hand the Free Peoples player cannot discard 3, so Desperate Defense adds its burden as
		// Aragorn is assigned: a second threat.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brute);
		scn.ShadowDeclineAssignments();
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ABurdenAShadowPossessionAddsAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(denethor, aragorn, knight);

		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Aragorn has one vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertAttachedTo(spear, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// 15 against 8: the Spear's bearer kills Aragorn, and the Spear adds a burden.  The threat Denethor adds for it
		// comes after the death, so no threat wound is placed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenOrThreatsASiteAddsAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(denethor, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		scn.SkipToSite(4);
		assertEquals("Eregion Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		scn.SkipToSite(5);
		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
