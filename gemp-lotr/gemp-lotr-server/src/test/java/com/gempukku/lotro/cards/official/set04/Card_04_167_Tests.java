package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_167_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillage", "4_167");
					put("pillage2", "4_167");

					// Uruk Trooper: an [isengard] Uruk-hai, strength 9, damage +1; "Regroup: Stack this minion on a site you
					// control"; its Shadow play from the stack is never taken
					put("trooper", "4_199");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai; damage +1 and nothing else
					put("savage2", "1_151");
					// Lurtz, Halfling Hunter: an [uruk-hai] Uruk-hai, here only stacked
					put("lurtz", "18_118");
					// Isengard Shaman: an [isengard] Orc (the culture without the race), here only stacked
					put("shaman", "3_59");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					// Ferocity: [isengard] Skirmish event, cost 0, "Make an Uruk-hai strength +2 (or +3 if at a battleground)"
					put("ferocity", "4_151");
					// Gollum, Old Villain: "Skirmish: Discard a card from hand to make Gollum strength +1 (or +2 if
					// skirmishing a Ring-bound companion)"; the ability needs no skirmish of his own
					put("gollum", "6_40");
					put("chaff", "1_3");        // Axe Strike: only a card in the Shadow player's hand

					// Aragorn, Ranger of the North: an unbound [gondor] Man, strength 8, vitality 4; a maneuver ability never used
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3, damage +1; "Skirmish: Exert Gimli to make him strength +2"
					put("gimli", "1_13");
					// War and Valor: [gondor] Skirmish event, cost 0, "Make a [gondor] Man strength +2 (or +3 and damage +1 if
					// skirmishing a roaming minion)"
					put("valor", "4_135");
					// Dwarven Bracers: on a Dwarf, "Response: If bearer is about to take a wound, discard this possession to
					// prevent that wound"
					put("bracers", "2_3");
					// Gondor Will See It Done: [gondor] Response event, cost 0, "If a companion is about to take a wound, exert a
					// [gondor] companion to prevent that wound"
					put("gondorwill", "2_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PillageOfRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Pillage of Rohan
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays on a site you control.<br>While an Uruk-hai is stacked on this site, the Free Peoples player may not play skirmish events or use skirmish special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillage");

		assertEquals("Pillage of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnASiteItsPlayerControlsAndOnNoOtherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillage = scn.GetShadowCard("pillage");

		scn.StartGame();
		// Both markers at site 4: sites 1 to 3 are behind them.
		scn.SkipToSite(4);
		scn.MoveCardsToHand(pillage);
		assertInHand(pillage);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		// The other player controls site 3, from an earlier turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));

		scn.FreepsPass();
		var site5 = scn.GetSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowPlayAvailable(pillage));

		scn.ShadowPlayCard(pillage);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		// The other player's site, a site on the path nobody controls, and the site the fellowship is at.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3, site4, site5));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		assertAttachedTo(pillage, site2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceAnUrukHaiIsStackedOnItsSiteTheFreePeoplesPlayerCanPlayNoSkirmishEventAndUseNoSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(valor);

		var pillage = scn.GetShadowCard("pillage");
		var trooper = scn.GetShadowCard("trooper");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Both markers at site 2: site 1 is behind them.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, pillage);
		assertTrue(scn.ShadowControls(site1));
		assertAttachedTo(pillage, site1);
		assertInHand(valor);

		// No minion at site 3, so straight to the regroup phase, where the Trooper stands.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(trooper);
		scn.FreepsPass();
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.ShadowActionAvailable(trooper));
		scn.ShadowUseCardAction(trooper);
		assertTrue(scn.GetStackedCards(site1).contains(trooper));

		// The fellowship moves again, to site 4, and the Troop is there to fight Aragorn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(valor));
		assertFalse(scn.FreepsActionAvailable(gimli));
	}

	@Test
	public void WithNoUrukHaiOnItsOwnSiteTheFreePeoplesPlayerPlaysASkirmishEventAndUsesASkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(valor);

		var pillage = scn.GetShadowCard("pillage");
		var pillage2 = scn.GetShadowCard("pillage2");
		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Both markers at site 4: sites 1 to 3 are behind them.
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		// On the Pillage's site, an [isengard] Orc; an Uruk-hai on a site of his with no Pillage; the second copy on a
		// site with nothing stacked.
		scn.AttachCardsTo(site1, pillage);
		scn.StackCardsOn(site1, shaman);
		scn.StackCardsOn(site2, savage);
		scn.AttachCardsTo(site3, pillage2);
		assertAttachedTo(pillage, site1);
		assertAttachedTo(pillage2, site3);
		assertTrue(scn.GetStackedCards(site1).contains(shaman));
		assertTrue(scn.GetStackedCards(site2).contains(savage));
		assertTrue(scn.GetStackedCards(site3).isEmpty());
		assertInHand(valor);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsPlayAvailable(valor));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(valor);
		// +2: the Troop (site 4) is not roaming at site 5.
		assertEquals(10, scn.GetStrength(aragorn));
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnUrukHaiOfAnotherCultureStackedTheBarHoldsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(valor);

		var pillage = scn.GetShadowCard("pillage");
		var lurtz = scn.GetShadowCard("lurtz");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, pillage);
		scn.StackCardsOn(site1, lurtz);
		assertAttachedTo(pillage, site1);
		assertTrue(scn.GetStackedCards(site1).contains(lurtz));
		assertInHand(valor);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(valor));
		assertFalse(scn.FreepsActionAvailable(gimli));
	}

	@Test
	public void UnderTheBarTheShadowPlayerAndFreePeoplesResponsesAreNotBarred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor = scn.GetFreepsCard("valor");
		var bracers = scn.GetFreepsCard("bracers");
		var gondorwill = scn.GetFreepsCard("gondorwill");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, bracers);
		scn.MoveCardsToHand(valor, gondorwill);

		var pillage = scn.GetShadowCard("pillage");
		var savage = scn.GetShadowCard("savage");
		var trooper = scn.GetShadowCard("trooper");
		var gollum = scn.GetShadowCard("gollum");
		var ferocity = scn.GetShadowCard("ferocity");
		var chaff = scn.GetShadowCard("chaff");
		scn.MoveCardsToHand(ferocity, chaff);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(site1, pillage);
		scn.StackCardsOn(site1, savage);
		assertAttachedTo(pillage, site1);
		assertTrue(scn.GetStackedCards(site1).contains(savage));
		assertAttachedTo(bracers, gimli);
		assertInHand(valor, gondorwill, ferocity, chaff);

		scn.MoveMinionsToTable(trooper, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, trooper},
				new PhysicalCardImpl[]{aragorn, gollum});
		scn.FreepsResolveSkirmish(gimli);

		// The bar holds for the Free Peoples player.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(valor));
		assertFalse(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();

		// The Shadow player's skirmish event and skirmish special ability.
		assertTrue(scn.ShadowPlayAvailable(ferocity));
		assertTrue(scn.ShadowActionAvailable(gollum));
		assertEquals(9, scn.GetStrength(trooper));
		// The Trooper is the only Uruk-hai in play (the Savage is stacked), so it is chosen unasked.
		scn.ShadowPlayCard(ferocity);
		assertEquals(11, scn.GetStrength(trooper));
		scn.FreepsPass();
		int gollumStrength = scn.GetStrength(gollum);
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChooseCard(chaff);
		assertInDiscard(chaff);
		assertEquals(gollumStrength + 1, scn.GetStrength(gollum));
		scn.PassCurrentPhaseActions();

		// The Trooper (11, damage +1) beats Gimli (6): two wounds.  The Free Peoples Response event and Response
		// ability are each offered against them.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gondorwill));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsChooseOptionalTrigger(gondorwill);
		assertInDiscard(gondorwill);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsChooseOptionalTrigger(bracers);
		assertInDiscard(bracers);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
