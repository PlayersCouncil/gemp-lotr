package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("folk", "4_34");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("brigand2", "4_10");
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("nodefense", "4_28");   // No Defense: another [dunland] condition that takes [dunland] tokens, by a Shadow ability of its own
					put("hides", "4_19");       // Hides: a [dunland] possession in the Shadow support area; its response answers a wound to a [dunland] Man
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, strength 8, vitality 4; may not be assigned to a skirmish
					put("staff", "4_174");      // Saruman's Staff: on Saruman, strength +2, fierce, damage +1; its maneuver ability lets him be assigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("rangersword", "1_112");// Ranger's Sword: strength +2 and damage +1, on Aragorn
					put("shield", "1_107");     // Great Shield: a [gondor] possession a Man bears; the minion archery total is -1
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession; bearer is not overwhelmed short of triple his strength
					put("goldberry", "9_51");   // Goldberry: an ally whose home is site 2 of the Fellowship path; strength 4; her texts need a river
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally; heals allies of site 3 as his turn starts
					put("elvensword", "4_64");  // Elven Sword: an [elven] possession an Elf bears; a skirmish ability used only on purpose
					put("vilya", "3_27");       // Vilya: a Free Peoples artifact, on Elrond; a maneuver ability used only on purpose
					put("toby", "1_305");       // Old Toby: a [shire] possession in the support area; its text acts only as it is played
					put("toby2", "1_305");
					put("lordOfMoria", "1_21"); // Lord of Moria: a Free Peoples condition in the support area (each Dwarf is damage +1)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; a test lays it as the site after the first.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("folk", "4_34");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SecretFolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Secret Folk
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each time a companion or ally loses a skirmish involving a [dunland] Man, you may place a [dunland] token on this card.<br><b>Maneuver:</b> Discard a Free Peoples possession for each [dunland] token here (limit 3). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("folk");

		assertEquals("Secret Folk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachCompanionWhoLosesToADunlandManOffersATokenAndDecliningPlacesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, aragorn, guard);
		scn.AttachCardsTo(guard, armor);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, brigand},
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{guard, brigand2});

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		// Nobody has lost anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(folk));

		// 8 against 6: Gimli loses to a Dunlending Brigand.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(folk));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(folk));

		// 11 against 8: Aragorn loses to a Dunlending Savage, and a second token is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(folk));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(folk));

		// 8 against 4, and the Armor keeps the Guard from being overwhelmed: he loses, and the offer is declined.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(folk));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(folk));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AllyLosingToDunlandManLetsShadowAddAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		scn.MoveAlliesToTable(goldberry);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		// Site 2 is Goldberry's home, so the Free Peoples player may assign the Brigand to her.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(goldberry, brigand);
		scn.FreepsResolveSkirmish(goldberry);
		// 8 against 4: she is overwhelmed, and has lost the skirmish all the same.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, goldberry);

		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(folk));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(folk));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheCompanionWinsNorWhenHeLosesToAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.AttachCardsTo(guard, armor);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, axeman}, new PhysicalCardImpl[]{aragorn, brigand});

		// 6 against 4: the Guard loses to an Easterling Axeman, a Man who is not [dunland].
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		// The Shadow player is asked nothing: where a token is offered, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 8 + 2 for the sword: Aragorn wins, and it is the [dunland] Man who has lost.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, brigand);
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheCompanionLosesToADunlandMinionWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var folk = scn.GetShadowCard("folk");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		// The Staff's maneuver ability makes "Saruman may not be assigned to a skirmish" not apply until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);

		// 8 + 2 for the Staff against 8: Aragorn loses a skirmish involving a [dunland] minion who is no Man.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 1 for losing + 1 for the Staff's damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The Shadow player is asked nothing: where a token is offered, he is asked here.  The Staff makes Saruman
		// fierce, so the game goes on to the fierce assignments.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		assertEquals(0, scn.GetCultureTokensOn(folk));
	}

	@Test
	public void WhereNoCultureTokenCanBeAddedTheTokenIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.FreepsResolveSkirmish(gimli);

		// 8 against 6: Gimli loses to a [dunland] Man, as he does where the token is offered.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(folk));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityDiscardsAFreePeoplesPossessionForEachTokenHereAndThenItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var elrond = scn.GetFreepsCard("elrond");
		var elvensword = scn.GetFreepsCard("elvensword");
		var vilya = scn.GetFreepsCard("vilya");
		var toby = scn.GetFreepsCard("toby");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(elrond);
		// Possessions of four cultures: on two companions, on an ally and in the support area.
		scn.AttachCardsTo(aragorn, shield);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(elrond, elvensword, vilya);
		scn.MoveCardsToSupportArea(toby, lordOfMoria);

		var folk = scn.GetShadowCard("folk");
		var nodefense = scn.GetShadowCard("nodefense");
		var hides = scn.GetShadowCard("hides");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk, nodefense, hides);

		scn.StartGame();
		scn.AddTokensToCard(folk, 2);
		// [dunland] tokens that are not "here": were they counted, the ability would take 3 possessions.
		scn.AddTokensToCard(nodefense, 3);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowActionAvailable(folk));
		scn.ShadowUseCardAction(folk);

		// 2 tokens: 2 of the 4 possessions.  A Free Peoples artifact and condition, and a Shadow possession, are no choices.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shield, armor, elvensword, toby));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(vilya, lordOfMoria, hides));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(elvensword, toby);

		assertInZone(Zone.DISCARD, elvensword, toby);
		assertAttachedTo(shield, aragorn);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(vilya, elrond);
		assertInZone(Zone.SUPPORT, lordOfMoria, hides, nodefense);
		assertEquals(3, scn.GetCultureTokensOn(nodefense));
		assertInZone(Zone.DISCARD, folk);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFourTokensThreePossessionsAreDiscardedAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var elrond = scn.GetFreepsCard("elrond");
		var elvensword = scn.GetFreepsCard("elvensword");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, shield);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(elrond, elvensword);
		scn.MoveCardsToSupportArea(toby, toby2);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.AddTokensToCard(folk, 4);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetCultureTokensOn(folk));
		scn.ShadowUseCardAction(folk);

		// 4 tokens and 5 possessions, limit 3: exactly 3 are to be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shield, armor, elvensword, toby, toby2));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(shield, armor, toby);

		assertInZone(Zone.DISCARD, shield, armor, toby);
		assertAttachedTo(elvensword, elrond);
		assertInZone(Zone.SUPPORT, toby2);
		assertInZone(Zone.DISCARD, folk);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoTokenItDiscardsOnlyItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, shield);
		scn.MoveCardsToSupportArea(toby);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// "Discard this condition" is done whatever the count, so the ability is on offer with no token.
		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowActionAvailable(folk));
		scn.ShadowUseCardAction(folk);

		assertAttachedTo(shield, aragorn);
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.DISCARD, folk);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, shield);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);

		scn.StartGame();
		scn.AddTokensToCard(folk, 1);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(folk));

		// Both players pass the maneuver phase; the archery phase is the next in which the Shadow player is asked.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(folk));
		assertInZone(Zone.SUPPORT, folk);
	}
}
