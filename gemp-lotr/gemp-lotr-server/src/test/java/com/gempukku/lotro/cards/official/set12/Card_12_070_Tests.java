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

public class Card_12_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hemmed1", "12_70");
					put("hemmed2", "12_70");

					// Axeman of Harad: a [men] Man, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Saruman, Coldly Still: a [men] minion who is no Man (Wizard), strength 8, vitality 4.  His +2 is
					// for [men] minions bearing a possession, and none does; his skirmish ability is never used.
					put("saruman", "17_48");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, site 2.  His abilities are never used.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");
					// Harmless: a [men] condition in the support area.  Its response is to a lost skirmish, never reached here.
					put("harmless", "13_93");
					// Spider Poison: a [gollum] Shadow possession, cheated onto a companion.  Its bearer exerts as he is
					// assigned, and nobody here is assigned.
					put("poison", "8_28");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Ranger's Sword: a possession, strength +2, "He is damage +1", and its bearer must be Aragorn.
					put("sword", "1_112");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3.  "Skirmish: Exert Gimli to make him
					// strength +2": a strength bonus that is not from a possession.
					put("gimli", "1_13");
					// Dwarven Axe: a possession, strength +2, bearer must be a Dwarf.  Its trigger on a minion losing to
					// its bearer only mills the Shadow player's deck.
					put("axe1", "1_9");
					put("axe2", "1_9");
					// Ring of Accretion: an ARTIFACT, strength +1, bearer must be a Dwarf.  Its maneuver ability is never used.
					put("accretion", "9_6");
					// Ring of Fury: an ARTIFACT, strength +1, bearer must be a Dwarf.  Its regroup ability is never used.
					put("fury", "9_7");
					// Boromir, Son of Denethor: a [gondor] Man, strength 7.  A skirmish ability, never used.
					put("boromir", "1_97");
					// Flaming Brand: a possession, strength +1, bearer must be a Man; may be borne beside another hand weapon.
					put("brand1", "2_32");
					put("brand2", "2_32");
					// Dwarf Guard: a [dwarven] Dwarf companion, strength 4, no text but a requirement to play.
					put("guard1", "1_7");
					put("guard2", "1_7");
					// Pippin, Friend to Frodo and Sam, Son of Hamfast: Hobbit companions with no hand weapon.  Pippin's
					// text is about [shire] tales; Sam's abilities are never used.
					put("pippin", "1_306");
					put("sam", "1_311");
					// Hobbit Sword: a [shire] possession of cost 1, strength +2, bearer must be a Hobbit.
					put("hobbitsword", "1_299");
					// Old Toby: a Free Peoples pipeweed possession in the support area; its draw is on its own play.
					put("toby", "1_305");
					// Thrarin, Dwarven Smith: a Dwarf ally, home site 3, strength 4, vitality 2.  His maneuver ability
					// lets him take part in skirmishes until the regroup phase.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HemmedInStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Hemmed In
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 6 companions, each companion skirmishing a [men] minion loses all strength bonuses from possessions.<br><b>Maneuver:</b> Spot your [men] minion and discard this condition to transfer a Free Peoples possession borne by a companion to another eligible bearer.
		*/

		var scn = GetScenario();

		var hemmed = scn.GetFreepsCard("hemmed1");

		assertEquals("Hemmed In", hemmed.getBlueprint().getTitle());
		assertNull(hemmed.getBlueprint().getSubtitle());
		assertFalse(hemmed.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hemmed.getBlueprint().getSide());
		assertEquals(Culture.MEN, hemmed.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, hemmed.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(hemmed, Keyword.SUPPORT_AREA));
		assertEquals(1, hemmed.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithSixCompanionsEachCompanionSkirmishingAMenMinionLosesOnlyTheStrengthBonusesOfHisPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var axe1 = scn.GetFreepsCard("axe1");
		var accretion = scn.GetFreepsCard("accretion");
		var boromir = scn.GetFreepsCard("boromir");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		// Frodo and five more: six companions.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, guard1, guard2);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe1, accretion);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var hemmed2 = scn.GetShadowCard("hemmed2");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(hemmed1, hemmed2);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, saruman);
		assertInZone(Zone.SUPPORT, hemmed1, hemmed2);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(axe1, gimli);
		assertAttachedTo(accretion, gimli);
		// 8 + 2 from the Sword, and its damage +1.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// 6 + 2 from the Axe + 1 from the Ring.
		assertEquals(9, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, axeman},
				new PhysicalCardImpl[]{gimli, saruman});

		// Assigned, and not yet skirmishing.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(gimli));

		scn.FreepsResolveSkirmish(gimli);
		// Skirmishing Saruman, a [men] Wizard: the Axe's +2 is gone; the Ring is an artifact and its +1 stays.
		assertEquals(7, scn.GetStrength(gimli));
		// Aragorn is still waiting for his skirmish.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// A strength bonus that is not from a possession is not lost.
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(gimli));

		// 9 against 8: Gimli wins, and his printed damage +1 and the Ring's place 3 wounds.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(saruman));

		scn.FreepsResolveSkirmish(aragorn);
		// Skirmishing the Axeman: the Sword's +2 is gone, taken once with two copies in play, and its damage
		// bonus is not a strength bonus.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 11 against 8: Aragorn loses, and in the regroup phase has the Sword's bonus again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));
	}

	@Test
	public void WithFiveCompanionsAndAnAllyEveryBonusStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var axe1 = scn.GetFreepsCard("axe1");
		var boromir = scn.GetFreepsCard("boromir");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Frodo and five more: six companions, and an ally beside them.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, guard1, guard2);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe1);
		// Vitality 3 with 2 wounds: one more kills Gimli.
		scn.AddWoundsToChar(gimli, 2);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(hemmed1);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, saruman);
		assertInZone(Zone.SUPPORT, hemmed1);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(axe1, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, axeman},
				new PhysicalCardImpl[]{gimli, saruman});

		// Six companions: skirmishing Saruman, Gimli loses the Axe's +2.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));

		// 8 against 6: Gimli is killed, and five companions are left beside the ally.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);

		// With five, Aragorn keeps the Sword's +2 while skirmishing the Axeman.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void ACompanionSkirmishingAMinionOfAnotherCultureAndAnAllySkirmishingAMenMinionKeepTheirBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		var axe2 = scn.GetFreepsCard("axe2");
		// Frodo and five more: six companions.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, guard1, guard2);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(thrarin, axe2);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var axeman = scn.GetShadowCard("axeman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(hemmed1);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, brigand);
		assertInZone(Zone.SUPPORT, hemmed1);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(axe2, thrarin);
		assertEquals(10, scn.GetStrength(aragorn));
		// 4 + 2 from the Axe.
		assertEquals(6, scn.GetStrength(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, brigand},
				new PhysicalCardImpl[]{thrarin, axeman});

		// A [dunland] Man is no [men] minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertInDiscard(brigand);

		// An ally skirmishing a [men] minion is no companion.
		scn.FreepsResolveSkirmish(thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(thrarin));
	}

	@Test
	public void ManeuverAbilityDiscardsThisConditionToTransferAFreePeoplesPossessionBorneByACompanionToAnotherEligibleBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var boromir = scn.GetFreepsCard("boromir");
		var brand1 = scn.GetFreepsCard("brand1");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var fury = scn.GetFreepsCard("fury");
		var guard1 = scn.GetFreepsCard("guard1");
		var thrarin = scn.GetFreepsCard("thrarin");
		var axe1 = scn.GetFreepsCard("axe1");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(sam, pippin, boromir, aragorn, gimli, guard1);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(toby);
		scn.AttachCardsTo(pippin, hobbitsword);
		scn.AttachCardsTo(boromir, brand1);
		scn.AttachCardsTo(aragorn, sword);
		// An artifact on a companion, with another Dwarf to bear it.
		scn.AttachCardsTo(gimli, fury);
		// A Free Peoples possession that an ally bears, with Dwarves among the companions to bear it.
		scn.AttachCardsTo(thrarin, axe1);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var hemmed2 = scn.GetShadowCard("hemmed2");
		var axeman = scn.GetShadowCard("axeman");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(hemmed1, hemmed2);
		// A Shadow possession that a companion bears.
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		// A Free Peoples possession that a minion bears.
		scn.AttachCardsTo(axeman, brand2);
		assertAttachedTo(hobbitsword, pippin);
		assertAttachedTo(brand1, boromir);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(fury, gimli);
		assertAttachedTo(axe1, thrarin);
		assertAttachedTo(brand2, axeman);
		assertInZone(Zone.SUPPORT, toby, hemmed1, hemmed2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(hemmed1));

		scn.ShadowUseCardAction(hemmed1);
		assertInDiscard(hemmed1);
		assertInZone(Zone.SUPPORT, hemmed2);

		// Free Peoples possessions borne by companions; not the Sword only Aragorn may bear, the artifact, the
		// Shadow possession, the possession in the support area, nor those an ally or a minion bears.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hobbitsword, brand1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, fury, poison, toby, axe1, brand2));
		scn.ShadowChooseCard(hobbitsword);

		// The other Hobbits; not its bearer, nor the Dwarf.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pippin, gimli, axeman));
		scn.ShadowChooseCard(sam);

		assertAttachedTo(hobbitsword, sam);
		assertNotAttachedTo(hobbitsword, pippin);
		// The Hobbit Sword costs 1, and nothing is paid for a transfer to another eligible bearer.
		assertEquals(pool, scn.GetTwilight());
		assertAttachedTo(brand1, boromir);
		assertAttachedTo(axe1, thrarin);
		assertAttachedTo(fury, gimli);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void FlamingBrandMayBeTransferredToAnotherManOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var brand1 = scn.GetFreepsCard("brand1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(boromir, aragorn, gimli);
		scn.AttachCardsTo(boromir, brand1);
		scn.AttachCardsTo(aragorn, sword);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(hemmed1);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		assertAttachedTo(brand1, boromir);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, hemmed1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(hemmed1);
		assertInDiscard(hemmed1);

		// The Brand is the one card to move.  Its bearer must be a Man: Aragorn, who may bear it beside his Sword,
		// and the Axeman; not Boromir, its bearer, the Dwarf or the Hobbit.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, gimli, frodo));
		scn.ShadowChooseCard(axeman);

		assertAttachedTo(brand1, axeman);
		assertNotAttachedTo(brand1, boromir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAllyAtHisHomeSiteIsAnEligibleBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe1 = scn.GetFreepsCard("axe1");
		var guard1 = scn.GetFreepsCard("guard1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(gimli, axe1);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(hemmed1);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(axeman);
		assertAttachedTo(axe1, gimli);
		assertInZone(Zone.SUPPORT, hemmed1);
		// The move takes the fellowship to site 3, Thrarin's home.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		scn.ShadowUseCardAction(hemmed1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(thrarin, guard1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli));
		scn.ShadowChooseCard(thrarin);

		assertAttachedTo(axe1, thrarin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAllyAwayFromHisHomeSiteIsAnEligibleBearerToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe1 = scn.GetFreepsCard("axe1");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli, guard1, guard2);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(gimli, axe1);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(hemmed1);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		assertAttachedTo(axe1, gimli);
		assertInZone(Zone.SUPPORT, hemmed1);
		// Site 2: Thrarin's home is site 3.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		scn.ShadowUseCardAction(hemmed1);
		// Thrarin, at his home site 3 and not at site 2, beside the two Dwarf Guards: a card's "transfer" is not
		// the Fellowship-phase transfer, and the same-site rule is that action's alone (Ketura's ruling on Q-0377).
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard1, guard2, thrarin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli));
		scn.ShadowChooseCard(thrarin);

		assertAttachedTo(axe1, thrarin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutAMenMinionToSpotTheManeuverAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe1 = scn.GetFreepsCard("axe1");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(gimli, guard1);
		scn.AttachCardsTo(gimli, axe1);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var harmless = scn.GetShadowCard("harmless");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(hemmed1, harmless);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertAttachedTo(axe1, gimli);
		assertInZone(Zone.SUPPORT, hemmed1, harmless);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [dunland] Man minion, and [men] cards that are no minion.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hemmed1));
	}

	@Test
	public void WithAMenHobbitToSpotAndNoPossessionToMoveTheConditionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var fury = scn.GetFreepsCard("fury");
		var guard1 = scn.GetFreepsCard("guard1");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToSupportArea(toby);
		scn.AttachCardsTo(gimli, fury);

		var hemmed1 = scn.GetShadowCard("hemmed1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToSupportArea(hemmed1);

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		assertAttachedTo(fury, gimli);
		assertInZone(Zone.SUPPORT, hemmed1, toby);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Ted Sandyman is a [men] minion who is a Hobbit.
		assertTrue(scn.ShadowActionAvailable(hemmed1));
		scn.ShadowUseCardAction(hemmed1);

		// An artifact on a companion and a possession in the support area: nothing to move.
		assertInDiscard(hemmed1);
		assertAttachedTo(fury, gimli);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
