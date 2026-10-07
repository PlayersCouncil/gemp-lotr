package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_057_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("harpoon1", "103_57");
					put("harpoon2", "103_57");
					put("buccaneer", "8_53");     // Corsair Buccaneer: corsair, vitality 2; its text is for when it is played
					put("freebooter", "8_54");    // Corsair Freebooter: corsair, vitality 2; its text is for when it is played
					put("plunderer", "8_58");     // Corsair Plunderer: corsair, vitality 2; its text is for when it is played
					put("gunners", "8_55");       // Corsair Gunners: corsair, vitality 3; a skirmish ability never used here
					put("lookout", "8_56");       // Corsair Lookout: corsair, vitality 1; its text is for when it is played
					put("axeman", "4_224");       // Easterling Axeman: [raider] Man who is no corsair, vitality 2; its text needs 2 burdens
					put("ships", "8_65");         // Ships of Great Draught: [raider] possession that holds [raider] tokens; Shadow and regroup abilities
					put("ships2", "8_65");
					put("sails", "8_50");         // Black Sails of Umbar: [raider] possession that holds [raider] tokens; a Shadow ability
					put("raiders", "4_242");      // Raiders From the East: [raider] condition that holds [raider] tokens. Maneuver: remove one here to add (1)
					put("larder", "8_23");        // Larder: [gollum] possession that holds [gollum] tokens; regroup and skirmish abilities
					put("poison", "8_28");        // Spider Poison: Shadow possession a companion bears; bearer exerts when assigned to a skirmish

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("grimir", "1_17");        // Grimir, Dwarven Elder: ally; a fellowship ability never used here
					put("athelas", "1_94");       // Athelas: possession, bearer must be a [gondor] Man; a fellowship ability
					put("athelas2", "1_94");
					put("anduril", "7_79");       // Anduril, Flame of the West: artifact, bearer must be Aragorn; strength +2, vitality +1
					put("stranger", "1_108");     // No Stranger to the Shadows: condition, bearer must be a ranger
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavageHarpoonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Savage Harpoon
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Strength: -3
		 * Game Text: Each time the fellowship moves, the Free Peoples player must hinder bearer or a Free Peoples
		 * 	item on bearer.
		 * 	Maneuver: Exert 2 corsairs and remove 2 [raider] tokens from a possession to transfer this from your
		 * 	support area to an unbound companion. Limit 1 per bearer.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("harpoon1");

		assertEquals("Savage Harpoon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-3, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var harpoon = scn.GetShadowCard("harpoon1");
		scn.MoveCardsToHand(harpoon);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, harpoon);
		assertTrue(scn.ShadowPlayAvailable(harpoon));

		scn.ShadowPlayCard(harpoon);

		assertEquals(3, scn.GetTwilight());
		// It goes to the support area with no bearer chosen, and weakens nobody there.
		assertInZone(Zone.SUPPORT, harpoon);
		assertNotAttachedTo(harpoon, aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheFellowshipMovesTheFreePeoplesPlayerMustHinderTheBearerOrAFreePeoplesItemOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		var stranger = scn.GetFreepsCard("stranger");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var harpoon = scn.GetShadowCard("harpoon1");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// On the bearer: a Free Peoples possession, a Free Peoples artifact, a Free Peoples condition and
		// another Shadow possession. On another companion: a Free Peoples possession.
		scn.AttachCardsTo(aragorn, harpoon, athelas, anduril, stranger, poison);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(anduril));

		scn.FreepsPass();

		// The Free Peoples player is the one asked.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsChoiceAvailable("Hinder <"));
		assertTrue(scn.FreepsChoiceAvailable("Hinder a Free Peoples item on"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());

		scn.FreepsChoose("Hinder a Free Peoples item on");

		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, anduril));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(harpoon, poison, stranger, athelas2, aragorn, boromir));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(anduril);

		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(harpoon));
		assertFalse(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// When the fellowship stays where it is, nothing more is asked: the turn ends and the other player's begins.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ChoosingTheBearerHindersHimAloneAndAtTheNextMoveAnItemOnHimMustBeHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		var harpoon = scn.GetShadowCard("harpoon1");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, harpoon, athelas, anduril);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.FreepsChoiceAvailable("Hinder <"));
		assertTrue(scn.FreepsChoiceAvailable("Hinder a Free Peoples item on"));
		assertFalse(scn.IsHindered(aragorn));

		// The first of the two options names the bearer alone.
		scn.FreepsChoose("Hinder <");

		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(harpoon));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsHindered(aragorn));

		scn.FreepsChooseToMove();

		// The bearer is hindered already, so one of the items he bears it must be.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsChoiceAvailable("Hinder <"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, anduril));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, boromir, harpoon));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(athelas);

		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(anduril));
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnItemAlreadyHinderedIsNotOfferedAgainAtTheNextMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		var harpoon = scn.GetShadowCard("harpoon1");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, harpoon, athelas, anduril);

		scn.StartGame();
		scn.FreepsPass();

		scn.FreepsChoose("Hinder a Free Peoples item on");
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, anduril));
		scn.FreepsChooseCard(athelas);

		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(anduril));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.IsHindered(athelas));
		scn.FreepsChooseToMove();

		// Asked again, with both options open: the bearer is not hindered and one item is left.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsChoiceAvailable("Hinder <"));
		assertTrue(scn.FreepsChoiceAvailable("Hinder a Free Peoples item on"));

		scn.FreepsChoose("Hinder a Free Peoples item on");

		// Anduril is the only item on him not yet hindered, so it is the one.
		assertTrue(scn.IsHindered(anduril));
		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(harpoon));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesItemOnTheBearerHeIsHinderedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var stranger = scn.GetFreepsCard("stranger");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var harpoon = scn.GetShadowCard("harpoon1");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// The bearer has a Free Peoples condition and a Shadow possession, and no Free Peoples item.
		scn.AttachCardsTo(aragorn, harpoon, stranger, poison);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();
		assertFalse(scn.IsHindered(aragorn));

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(harpoon));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(athelas2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheBearerHinderedAndNoFreePeoplesItemOnHimTheNextMoveAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var stranger = scn.GetFreepsCard("stranger");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var harpoon = scn.GetShadowCard("harpoon1");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, harpoon, stranger, poison);
		scn.AttachCardsTo(boromir, athelas2);

		scn.StartGame();
		scn.FreepsPass();

		// The bearer has no Free Peoples item, so he is hindered at the first move.
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.IsHindered(aragorn));
		scn.FreepsChooseToMove();

		// Neither he nor anything he bears can be hindered now, and so nothing is asked.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(harpoon));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(athelas2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheSupportAreaItAsksNothingWhenTheFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var harpoon = scn.GetShadowCard("harpoon1");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToSupportArea(harpoon);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(athelas));
		assertInZone(Zone.SUPPORT, harpoon);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoCopiesAsksAboutItsOwnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var harpoon1 = scn.GetShadowCard("harpoon1");
		var harpoon2 = scn.GetShadowCard("harpoon2");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, harpoon1, athelas);
		scn.AttachCardsTo(boromir, harpoon2, athelas2);

		scn.StartGame();
		scn.FreepsPass();

		// One copy asks, and the one item on its own bearer is hindered without a further choice...
		assertTrue(scn.FreepsChoiceAvailable("Hinder a Free Peoples item on"));
		scn.FreepsChoose("Hinder a Free Peoples item on");
		assertNotEquals(scn.IsHindered(athelas), scn.IsHindered(athelas2));

		// ...and then the other copy asks the same of its own.
		assertTrue(scn.FreepsChoiceAvailable("Hinder a Free Peoples item on"));
		scn.FreepsChoose("Hinder a Free Peoples item on");

		assertTrue(scn.IsHindered(athelas));
		assertTrue(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionExertsTwoCorsairsAndRemovesTwoRaiderTokensFromAPossessionToTransferItToAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(grimir);

		var harpoon = scn.GetShadowCard("harpoon1");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var ships2 = scn.GetShadowCard("ships2");
		var raiders = scn.GetShadowCard("raiders");
		var larder = scn.GetShadowCard("larder");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var freebooter = scn.GetShadowCard("freebooter");
		var plunderer = scn.GetShadowCard("plunderer");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(harpoon, ships, sails, ships2, raiders, larder);
		// Two possessions with [raider] tokens enough, one with a single token, a condition with [raider]
		// tokens and a possession with tokens of another culture.
		scn.AddTokensToCard(ships, 3);
		scn.AddTokensToCard(sails, 2);
		scn.AddTokensToCard(ships2, 1);
		scn.AddTokensToCard(raiders, 3);
		scn.AddTokensToCard(larder, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer, freebooter, plunderer, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 4 companions + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, harpoon);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowActionAvailable(harpoon));

		scn.ShadowUseCardAction(harpoon);

		// Two of the three corsairs; the Easterling is no corsair.
		assertTrue(scn.ShadowHasCardChoiceAvailable(buccaneer, freebooter, plunderer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(buccaneer, plunderer);

		assertEquals(1, scn.GetWoundsOn(buccaneer));
		assertEquals(1, scn.GetWoundsOn(plunderer));
		assertEquals(0, scn.GetWoundsOn(freebooter));
		assertEquals(0, scn.GetWoundsOn(axeman));

		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, sails));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships2, raiders, larder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(ships);

		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(sails));
		assertEquals(1, scn.GetCultureTokensOn(ships2));
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertEquals(3, scn.GetCultureTokensOn(larder));

		// The unbound companions. Frodo and Sam are Ring-bound, and Grimir is an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, grimir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(harpoon, gimli);
		assertNotAttachedTo(harpoon, aragorn);
		assertInZone(Zone.ATTACHED, harpoon);
		// Gimli 6 - 3 for the Harpoon
		assertEquals(3, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		// A transfer by a card's text costs no twilight.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceItIsOnACompanionAndASecondCopyDoesNotGoToACompanionWhoBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var harpoon1 = scn.GetShadowCard("harpoon1");
		var harpoon2 = scn.GetShadowCard("harpoon2");
		var ships = scn.GetShadowCard("ships");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var freebooter = scn.GetShadowCard("freebooter");
		var plunderer = scn.GetShadowCard("plunderer");
		var gunners = scn.GetShadowCard("gunners");
		scn.MoveCardsToSupportArea(harpoon1, harpoon2, ships);
		scn.AddTokensToCard(ships, 5);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer, freebooter, plunderer, gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(harpoon1));
		assertTrue(scn.ShadowActionAvailable(harpoon2));

		scn.ShadowUseCardAction(harpoon1);
		scn.ShadowChooseCards(buccaneer, freebooter);
		// The one possession with tokens pays without a choice: 5 - 2.
		assertEquals(3, scn.GetCultureTokensOn(ships));
		// With no Harpoon on anybody, every unbound companion is offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, boromir));
		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(harpoon1, aragorn);

		scn.FreepsPass();

		// The copy on Aragorn is no longer in the support area. Its costs could still be paid, and the copy
		// that is there still offers.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(harpoon1));
		assertTrue(scn.ShadowActionAvailable(harpoon2));

		scn.ShadowUseCardAction(harpoon2);
		// The Plunderer and the Gunners are the two corsairs still able to exert.
		assertEquals(1, scn.GetWoundsOn(plunderer));
		assertEquals(1, scn.GetWoundsOn(gunners));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(boromir);

		assertAttachedTo(harpoon1, aragorn);
		assertAttachedTo(harpoon2, boromir);
		// Aragorn 8 - 3, Boromir 7 - 3
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionLeftWhoBearsNoneItIsStillOfferedAndItsCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var harpoon1 = scn.GetShadowCard("harpoon1");
		var harpoon2 = scn.GetShadowCard("harpoon2");
		var ships = scn.GetShadowCard("ships");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var freebooter = scn.GetShadowCard("freebooter");
		scn.MoveCardsToSupportArea(harpoon2, ships);
		scn.AddTokensToCard(ships, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer, freebooter);
		scn.SkipToPhase(Phase.MANEUVER);
		// The one unbound companion already bears a Harpoon.
		scn.AttachCardsTo(aragorn, harpoon1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(harpoon2));

		scn.ShadowUseCardAction(harpoon2);

		assertEquals(1, scn.GetWoundsOn(buccaneer));
		assertEquals(1, scn.GetWoundsOn(freebooter));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.SUPPORT, harpoon2);
		assertNotAttachedTo(harpoon2, aragorn);
		assertNotAttachedTo(harpoon2, sam);
		assertAttachedTo(harpoon1, aragorn);
		// Aragorn 8 - 3 for the one Harpoon he bears
		assertEquals(5, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTwoCorsairsCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var harpoon = scn.GetShadowCard("harpoon1");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var gunners = scn.GetShadowCard("gunners");
		var lookout = scn.GetShadowCard("lookout");
		var axeman = scn.GetShadowCard("axeman");
		var plunderer = scn.GetShadowCard("plunderer");
		scn.MoveCardsToSupportArea(harpoon, ships, raiders);
		scn.AddTokensToCard(ships, 3);
		scn.AddTokensToCard(raiders, 3);

		scn.StartGame();
		// A corsair who could exert twice, a corsair who cannot exert at all, and a [raider] Man who is no corsair.
		scn.MoveMinionsToTable(gunners, lookout, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(3, scn.GetVitality(gunners));
		assertEquals(1, scn.GetVitality(lookout));
		assertFalse(scn.ShadowActionAvailable(harpoon));

		// Raiders From the East: remove a token there to add (1). It keeps the maneuver phase going.
		scn.ShadowUseCardAction(raiders);
		// A second corsair who can exert arrives.
		scn.MoveMinionsToTable(plunderer);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(harpoon));
		assertEquals(0, scn.GetWoundsOn(gunners));
		assertInZone(Zone.SUPPORT, harpoon);
	}

	@Test
	public void IsNotOfferedUntilOnePossessionHasTwoRaiderTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var harpoon = scn.GetShadowCard("harpoon1");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var raiders = scn.GetShadowCard("raiders");
		var larder = scn.GetShadowCard("larder");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var freebooter = scn.GetShadowCard("freebooter");
		scn.MoveCardsToSupportArea(harpoon, ships, sails, raiders, larder);
		// Two [raider] tokens on possessions, but one on each; three on a [raider] condition; three tokens of
		// another culture on a possession.
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(sails, 1);
		scn.AddTokensToCard(raiders, 3);
		scn.AddTokensToCard(larder, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer, freebooter);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(harpoon));

		// Raiders From the East: remove a token there to add (1). It keeps the maneuver phase going.
		scn.ShadowUseCardAction(raiders);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		// A second token on one of the possessions.
		scn.AddTokensToCard(ships, 1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.ShadowActionAvailable(harpoon));
		assertInZone(Zone.SUPPORT, harpoon);
	}
}
