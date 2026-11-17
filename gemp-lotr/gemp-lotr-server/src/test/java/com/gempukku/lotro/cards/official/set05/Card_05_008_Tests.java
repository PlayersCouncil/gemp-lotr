package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "5_8");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, damage +1 in print; a skirmish ability only
					put("farin", "1_11");       // Farin, Dwarven Emissary: a second Dwarf companion; strength +2 only against an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf, strength 8
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					// Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, damage +1 (and fierce) while 2 burdens are spotted
					put("nelya", "2_84");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; Gimli beats and kills it
					put("battlecry", "1_128");  // Lurtz's Battle Cry: Skirmish event, an Uruk-hai is strength -1 and damage +1
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul to spot; his text reaches a bearer of a [wraith]
					// condition, and none is here
					put("lemenya", "1_232");
					// A Shadow Fell Over Them: "Assignment: Spot a Nazgul and remove (4) to play a Nazgul from your
					// discard pile"; its other text answers a Nazgul winning a skirmish
					put("shadowfell", "15_182");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Uruk Camp: Battleground and Plains.  "Each time an Uruk-hai is played, that minion must exert":
					// no Uruk-hai is played here.
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Eastfold 4_331 (Plains, no battleground; its Shadow ability needs 3 [isengard] trackers) and site 4
	// is White Rocks 4_346 (Battleground and nothing else; its Shadow number counts mounted companions).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "5_8");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, damage +1 in print; a skirmish ability only
					put("farin", "1_11");       // Farin, Dwarven Emissary: a second Dwarf companion; strength +2 only against an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf, strength 8
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					// Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, damage +1 (and fierce) while 2 burdens are spotted
					put("nelya", "2_84");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; Gimli beats and kills it
					put("battlecry", "1_128");  // Lurtz's Battle Cry: Skirmish event, an Uruk-hai is strength -1 and damage +1
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul to spot; his text reaches a bearer of a [wraith]
					// condition, and none is here
					put("lemenya", "1_232");
					// A Shadow Fell Over Them: "Assignment: Spot a Nazgul and remove (4) to play a Nazgul from your
					// discard pile"; its other text answers a Nazgul winning a skirmish
					put("shadowfell", "15_182");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornofHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Horn of Helm
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Maneuver:</b> Exert a Dwarf companion to make each minion at a battleground lose all <b>damage</b> bonuses until the regroup phase.
		*/

		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");

		assertEquals("Horn of Helm", horn.getBlueprint().getTitle());
		assertNull(horn.getBlueprint().getSubtitle());
		assertTrue(horn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, horn.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, horn.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, horn.getBlueprint().getCardType());
		assertNull(horn.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(horn, Keyword.SUPPORT_AREA));
		assertEquals(2, horn.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtABattlegroundEachMinionLosesItsDamageBonusesAndNoCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(horn);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(party, nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.FreepsActionAvailable(horn));
		scn.FreepsUseCardAction(horn);

		// Dwarf companions of two titles; not a Dwarf ally, nor a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		// Minions only: Gimli keeps his.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtAnotherBattlegroundTheMinionsLoseTheirDamageBonusesToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var horn = scn.GetFreepsCard("horn");
		var gimli = scn.GetFreepsCard("gimli");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(horn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(horn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtASiteThatIsNoBattlegroundItIsPaidForAndTheMinionsKeepTheirBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var horn = scn.GetFreepsCard("horn");
		var gimli = scn.GetFreepsCard("gimli");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(horn);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.MANEUVER);

		// Eastfold: Plains, no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(horn));
		scn.FreepsUseCardAction(horn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyDwarfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(horn);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Gimli cannot exert.  The Dwarf ally and Aragorn could.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(party);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(horn));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
	}

	@Test
	public void TheBonusesStayLostThroughTheSkirmishesNoneIsGainedAndTheyAreBackInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		var runner = scn.GetShadowCard("runner");
		var battlecry = scn.GetShadowCard("battlecry");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(horn);
		scn.MoveCardsToHand(battlecry);

		scn.StartGame();
		assertInHand(battlecry);
		scn.MoveMinionsToTable(party, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		scn.FreepsUseCardAction(horn);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, party});

		// Gimli 6 against the Runner's 5: he wins and kills it.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// The skirmish after one already resolved: the Raiding Party still has no bonus, and the Battle Cry's
		// damage +1 does not land, though its strength -1 does.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(party));
		assertInHand(battlecry);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(battlecry));
		scn.ShadowPlayCard(battlecry);
		assertInDiscard(battlecry);
		assertEquals(8, scn.GetStrength(party));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		// 8 against 8: the Shadow side wins the tie, and Aragorn takes the one wound for losing and none for damage.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// At the first regroup decision the bonus is back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
	}

	@Test
	public void AMinionThatComesIntoPlayAfterTheAbilityIsUsedKeepsItsDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var gimli = scn.GetFreepsCard("gimli");
		var party = scn.GetShadowCard("party");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var shadowfell = scn.GetShadowCard("shadowfell");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(horn, shadowfell);
		scn.MoveCardsToDiscard(nelya);

		scn.StartGame();
		// Two burdens: Nelya is damage +1 whenever she is in play.
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(party, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInDiscard(nelya);
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		scn.FreepsUseCardAction(horn);
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// Lemenya is the Nazgul spotted, and Nelya the one Nazgul in the discard pile.
		assertTrue(scn.ShadowActionAvailable(shadowfell));
		scn.ShadowUseCardAction(shadowfell);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);

		// The Horn made the minions then in play lose their bonuses; Nelya came afterwards and keeps hers.
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

}
